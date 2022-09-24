import { defineStore, acceptHMRUpdate } from 'pinia'
import { ref } from 'vue'

export const useEventCategories = defineStore('eventCategories', () => {
  const eventCategories = ref([])

  const fetchEventCategories = async () => {
    try {
      const res = await fetch(`${import.meta.env.VITE_BASE_PATH}/api/categories`)
      if (res.status === 200) {
        const data = await res.json()
        eventCategories.value = data
      }
    } catch (err) {
      console.log(err)
    }
  }

  const fetchEventCategoryById = async (categoryId) => {
    const res = await fetch(
      `${import.meta.env.VITE_BASE_PATH}/api/categories/${categoryId}`
    )
    const data = await res.json()
    if (res.status === 200) {
      return data
    }
    throw data
  }

  const updateEventCategory = async (category) => {
    const res = await fetch(
      `${import.meta.env.VITE_BASE_PATH}/api/categories/${category.categoryId}`,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(category),
      }
    )
    const data = await res.json()
    if (res.status === 200) {
      return data
    }
    throw data
  }

  const getCategoryByCategoryId = (categoryId) => {
    return eventCategories.value.find((category) => category.categoryId === categoryId)
  }

  return {
    eventCategories,
    fetchEventCategories,
    fetchEventCategoryById,
    updateEventCategory,
    getCategoryByCategoryId,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useEventCategories, import.meta.hot))
}
