import { defineStore, acceptHMRUpdate } from 'pinia'
import { ref } from 'vue'

export const useEvents = defineStore('events', () => {
  const events = ref([])

  const addNewEvent = async (newEvent) => {
    const res = await fetch(`${import.meta.env.VITE_BASE_PATH}/api/events`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(newEvent),
    })
    const data = await res.json()
    if (res.status === 201) {
      await fetchEvents()
      return data
    } else if (res.status === 400) {
      throw data
    }
  }

  const updateEvent = async (bookingId, editedEvent) => {
    const res = await fetch(`${import.meta.env.VITE_BASE_PATH}/api/events/${bookingId}`, {
      method: 'PATCH',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(editedEvent),
    })

    if (res.status === 200) {
      await fetchEvents()
    } else if (res.status === 400) {
      const data = await res.json()
      throw data
    }
  }

  const deleteEventById = async (bookingId) => {
    const res = await fetch(`${import.meta.env.VITE_BASE_PATH}/api/events/${bookingId}`, {
      method: 'DELETE',
    })
    if (res.status === 200) {
      events.value = events.value.filter((event) => event.bookingId !== bookingId)
    } else {
      const data = await res.json()
      await fetchEvents()
      throw data
    }
  }

  const getEventDetailById = (bookingId) => {
    return events.value.find((event) => event.bookingId === bookingId)
  }

  const getEventsSameCategory = (searchEvent) => {
    return events.value.filter(
      (event) =>
        searchEvent.bookingId !== event.bookingId &&
        event.category.categoryId === searchEvent.category.categoryId
    )
  }

  const fetchEvents = async (filterOption) => {
    let requestParams = ''
    if (filterOption !== undefined && typeof filterOption === 'object') {
      requestParams = new URLSearchParams(filterOption)
    }
    const res = await fetch(
      `${import.meta.env.VITE_BASE_PATH}/api/events?${requestParams}`
    )
    const data = await res.json()
    if (res.status === 200) {
      events.value = data
    } else if (res.status === 404) {
      throw data
    }
  }

  return {
    events,
    fetchEvents,
    getEventDetailById,
    getEventsSameCategory,
    addNewEvent,
    updateEvent,
    deleteEventById,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useEvents, import.meta.hot))
}
