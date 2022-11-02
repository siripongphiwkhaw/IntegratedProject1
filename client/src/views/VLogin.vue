<script setup>
import { ref } from 'vue'
import moment from 'moment'
import { useUser } from '../stores/user'
import AppInput from '../components/App/AppInput.vue'
import ScheduleFormInput from '../components/VSchedules/ScheduleFormInput.vue'
import { useRouter } from 'vue-router'
import { useEventCategories } from '../stores/eventCategories'
import { useEvents } from '../stores/events'

const router = useRouter()
const userStore = useUser()
const eventStore = useEvents()
const categoryStore = useEventCategories()
const isLoading = ref(false)
const loginModelTemplate = {
  email: '',
  password: '',
}

const eventModelTemplate = {
  category: null,
  bookingName: '',
  bookingEmail: userStore.user.email || '',
  eventStartTime: '',
  eventNotes: '',
}

const formLogin = ref({ ...loginModelTemplate })
const errorMessage = ref({ ...loginModelTemplate })

const eventModel = ref({ ...eventModelTemplate })
const eventModelError = ref({ ...eventModelTemplate })

const scheduleFormInputModal = ref({
  state: false,
  show: async () => {
    if (categoryStore.eventCategories.length <= 0) {
      await categoryStore.fetchEventCategories()
    }
    scheduleFormInputModal.value.state = true
  },
  close: () => {
    resetEventForm()
    scheduleFormInputModal.value.state = false
  },
})

const addEvent = async () => {
  try {
    isLoading.value = true
    const data = await eventStore.addNewEventAsGuest({
      ...eventModel.value,
      categoryId: eventModel.value.category.categoryId,
      eventStartTime: moment(eventModel.value.eventStartTime).toISOString(),
    })
    alert(`Event added successfully (Booking ID: ${data.bookingId})`)
    scheduleFormInputModal.value.close()
  } catch (err) {
    const fieldErrors = err.fieldErrors
    for (let key in fieldErrors) {
      fieldErrors[key] = fieldErrors[key].join(', ')
    }
    eventModelError.value = fieldErrors
  } finally {
    isLoading.value = false
  }
}

const resetEventForm = () => {
  eventModel.value = { ...eventModelTemplate }
  eventModelError.value = {}
}

const loginUser = async () => {
  const passwordValid = validatePassword()

  if (!passwordValid) return

  try {
    isLoading.value = true
    await userStore.loginUser(formLogin.value)
    router.push('/')
  } catch (err) {
    errorMessage.value = {}
    if (err.status === 404) {
      errorMessage.value.email = err.message
    } else if (err.status === 401) {
      errorMessage.value.password = err.message
    }
  } finally {
    isLoading.value = false
  }
}

const validatePassword = () => {
  errorMessage.value.password = ''

  if (formLogin.value.password.length < 8 || formLogin.value.password.length > 14) {
    errorMessage.value.password = 'Password size must be between 8 and 14.'
    return false
  }

  return true
}
</script>

<template>
  <app-loading-screen v-if="isLoading" />
  <ScheduleFormInput
    :event-model="eventModel"
    :event-model-error="eventModelError"
    :modal-state="scheduleFormInputModal"
    @add-event="addEvent"
    @reset-form="resetEventForm"
  />
  <div class="container">
    <div class="wrapper">
      <div class="welcome-title">Welcome to OASIP-SSI2</div>
      <div class="welcome-desc">Please, fill form to login.</div>
      <form @submit.prevent="loginUser">
        <AppInput
          type="email"
          pattern="^[^(\.)][a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}"
          v-model.trim="formLogin.email"
          label-text="Email"
          :max-length="50"
          :error-message="errorMessage.email"
          :required="true"
        />
        <AppInput
          type="password"
          v-model="formLogin.password"
          label-text="Password"
          :minlength="8"
          :error-message="errorMessage.password"
          :required="true"
        />
        <div>
          <app-button class="btn-submit" type="submit">Login</app-button>
          <app-button class="btn-guest" type="button" @click="scheduleFormInputModal.show"
            >Create event without sign-in</app-button
          >
        </div>
      </form>
      <router-link class="register-href" to="/register">I don't have an account</router-link>
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 735px;
  width: 100%;
  margin: 0 auto;
  height: 100%;
}

.wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-height: 100%;
  margin: 0 auto;
}

.welcome-title {
  font-weight: 300;
}

.welcome-desc {
  font-weight: 300;
  font-size: 0.8em;
  margin: 5px auto;
  opacity: 0.7;
}

.register-href {
  font-size: 0.7em;
  margin-top: 10px;
}

input {
  width: 100%;
}

form label {
  font-size: 0.8em;
}

form .btn-submit {
  width: 100%;
  margin-top: 2em;
  margin-left: auto;
  margin-right: auto;
}

.btn-guest {
  margin: 0;
  padding: 5px;
  width: 100%;
  background-color: whitesmoke !important;
  color: black !important;
}

.btn-guest:hover {
  background-color: white !important;
}
</style>
