import moment from 'moment'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

import AppButton from './components/App/AppButton.vue'
import AppLoadingScreen from './components/App/AppLoadingScreen.vue'
import AppModal from './components/App/AppModal.vue'

/* import the fontawesome core */
import { library } from '@fortawesome/fontawesome-svg-core'

/* import specific icons */
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'

/* import font awesome icon component */
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'

library.add(fas, far)

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)

app.component('font-awesome-icon', FontAwesomeIcon)
app.component('app-button', AppButton)
app.component('app-loading-screen', AppLoadingScreen)
app.component('app-modal', AppModal)

app.config.globalProperties.$getFormattedEventPeriod = (time, duration) => {
  const startTime = moment(time)
  const finishTime = startTime.clone().add(duration, 'minutes')
  return `${startTime.format('LT')} - ${finishTime.format('LT')}`
}

app.config.globalProperties.$truncate = (str, maxLength) => {
  return str.length > maxLength ? `${str.substr(0, maxLength - 1)}\u2026` : str
}
app.mount('#app')
