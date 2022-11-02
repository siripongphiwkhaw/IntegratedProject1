import axios from 'axios'
import { defineStore, acceptHMRUpdate } from 'pinia'
import { ref } from 'vue'

export const useEvents = defineStore('events', () => {
  const events = ref([])

  const addNewEvent = async (newEvent) => {
    const formData = new FormData()
    for (let [key, value] of Object.entries(newEvent)) {
      formData.append(key, value)
    }
    try {
      const res = await axios.post(`${import.meta.env.VITE_BASE_PATH}/api/events`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      await fetchEvents()
      return res.data
    } catch (err) {
      throw err.response
    }
  }

  const addNewEventAsGuest = async (newEvent) => {
    try {
      const res = await axios.post(`${import.meta.env.VITE_BASE_PATH}/api/guests`, newEvent, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      return res.data
    } catch (err) {
      throw err.response.data
    }
  }

  const updateEvent = async (bookingId, editedEvent) => {
    const formData = new FormData()
    for (let [key, value] of Object.entries(editedEvent)) {
      formData.append(key, value)
    }
    try {
      await axios.patch(`${import.meta.env.VITE_BASE_PATH}/api/events/${bookingId}`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })
      await fetchEvents()
    } catch (err) {
      throw err.response.data
    }
  }

  const deleteEventById = async (bookingId) => {
    try {
      await axios.delete(`${import.meta.env.VITE_BASE_PATH}/api/events/${bookingId}`)
    } catch (err) {
      throw err.response.data
    } finally {
      await fetchEvents()
    }
  }

  const downloadFile = async (bookingId, file) => {
    try {
      axios({
        url: `${import.meta.env.VITE_BASE_PATH}/api/events/${bookingId}/file`,
        method: 'GET',
        responseType: 'blob',
      }).then((response) => {
        const url = window.URL.createObjectURL(new Blob([response.data]))
        const link = document.createElement('a')
        link.href = url
        link.setAttribute('download', file.name)
        document.body.appendChild(link)
        link.click()
      })
    } catch (err) {
      throw err.response.data
    }
  }

  const getEventDetailById = (bookingId) => {
    return events.value.find((event) => event.bookingId === bookingId)
  }

  const getEventsSameCategory = (searchEvent) => {
    return events.value.filter(
      (event) =>
        searchEvent.bookingId !== event.bookingId && event.category.categoryId === searchEvent.category.categoryId
    )
  }

  const fetchEvents = async (filterOption) => {
    let requestParams = ''
    if (filterOption !== undefined && typeof filterOption === 'object') {
      requestParams = new URLSearchParams(filterOption)
    }
    try {
      const res = await axios(`${import.meta.env.VITE_BASE_PATH}/api/events?${requestParams}`)
      events.value = res.data
    } catch (err) {
      throw err.response.data
    }
  }

  return {
    events,
    fetchEvents,
    getEventDetailById,
    getEventsSameCategory,
    addNewEvent,
    addNewEventAsGuest,
    updateEvent,
    deleteEventById,
    downloadFile,
  }
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useEvents, import.meta.hot))
}
