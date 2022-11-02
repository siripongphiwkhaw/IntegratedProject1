<script setup>
import Moment from 'moment'
import { extendMoment } from 'moment-range'
import { computed, onUpdated, ref } from 'vue'
import { useEventCategories } from '../../stores/eventCategories.js'
import { useEvents } from '../../stores/events.js'
import { useUser } from '../../stores/user'

const moment = extendMoment(Moment)

const MAX_FILE_SIZE = 10 * (1024 * 1024) // MB to byte
const fileRef = ref()

const categoryStore = useEventCategories()
const eventStore = useEvents()
const userStore = useUser()

const props = defineProps({
  modalState: {
    type: Object,
  },
  eventModel: {
    type: Object,
    default: {},
  },
  eventModelError: {
    type: Object,
    default: {},
  },
  isEditMode: {
    type: Boolean,
    default: false,
  },
})

const formConfig = ref({
  isLoading: true,
  maxLength: {
    bookingName: 100,
    bookingEmail: 50,
    eventNotes: 500,
  },
  minLength: {
    bookingName: 1,
    bookingEmail: 1,
  },
  min: {
    eventStartTime: moment().add(1, 'minutes').format('YYYY-MM-DDTHH:mm'),
    updateMinInterval: null,
  },
})

const openFileSelector = () => fileRef.value.click()

const onFileInputChanged = (evt) => {
  const file = evt.target.files[0]
  if (file.size > MAX_FILE_SIZE) {
    props.eventModelError.file = 'File size is exceed maximum 10Mb'
    return
  }
  props.eventModel.file = file
}

const removeFile = () => {
  fileRef.value.value = []
  props.eventModel.file = ''
  props.eventModelError.file = ''
}

const onInputFocusOut = (evt) => {
  if (evt.target.name === 'eventStartTime') {
    console.log(evt.target.validity.rangeUnderflow)
    if (evt.target.validity.rangeUnderflow) {
      evt.target.setCustomValidity('Must be future date and time')
    } else {
      const categoryEvents = eventStore.getEventsSameCategory(props.eventModel)
      const eventOverlap = categoryEvents.some((event) => isEventPeriodOverlap(props.eventModel, event))

      if (categoryEvents.length > 0 && eventOverlap) {
        evt.target.setCustomValidity('This time is already reserve')
      } else {
        evt.target.setCustomValidity('')
      }
    }
  }

  if (!evt.target.checkValidity()) {
    return (props.eventModelError[evt.target.name] = evt.target.validationMessage)
  }
  props.eventModelError[evt.target.name] = ''
}

const isEventPeriodOverlap = (eventA, eventB) => {
  const rangeEventA = moment.range(
    eventA.eventStartTime,
    moment(eventA.eventStartTime).clone().add(eventA.eventDuration, 'minutes')
  )

  const rangeEventB = moment.range(
    eventB.eventStartTime,
    moment(eventB.eventStartTime).clone().add(eventB.eventDuration, 'minutes')
  )

  return rangeEventA.overlaps(rangeEventB, { adjacent: false })
}

const visibleValidationErrorMsg = computed(() => {
  return {
    visibility: Object.keys(props.eventModelError).length > 0 ? 'visible' : 'hidden',
  }
})

onUpdated(async () => {
  // on modal opened
  if (props.modalState.state) {
    if (!props.eventModel.category) {
      props.eventModel.category = categoryStore.eventCategories[0]
    }

    formConfig.value.min.updateMinInterval = setInterval(() => {
      formConfig.value.min.eventStartTime = moment().add(1, 'minutes').format('YYYY-MM-DDTHH:mm')
    }, 1000)
  } else if (!props.modalState.state) {
    clearInterval(formConfig.value.min.updateMinInterval)
  }
})

defineEmits(['add-event', 'edit-event', 'reset-form'])
</script>

<template>
  <app-modal :show="modalState.state">
    <h3 style="text-align: center">{{ isEditMode ? 'Edit event' : 'Add new event' }}</h3>
    <form @submit.prevent="isEditMode ? $emit('edit-event') : $emit('add-event')" @reset.prevent="$emit('reset-form')">
      <div class="form-group" style="display: block">
        <div class="input-group">
          <label for="form-booking-name" class="required">Booking name</label>
          <input
            v-model.trim="eventModel.bookingName"
            id="form-booking-name"
            name="bookingName"
            style="width: 100%"
            type="text"
            @focusout="onInputFocusOut"
            :class="eventModelError.bookingName && 'invalid'"
            :minlength="formConfig.minLength.bookingName"
            :maxlength="formConfig.maxLength.bookingName"
            :disabled="isEditMode"
            required
          />
          <div class="input-validation-box">
            <div class="input-invalid-msg" :style="visibleValidationErrorMsg">
              {{ eventModelError.bookingName || '' }}
            </div>
            <div class="input-counter">{{ eventModel.bookingName.length }}/{{ formConfig.maxLength.bookingName }}</div>
          </div>
        </div>
      </div>
      <div class="input-group">
        <label for="form-booking-email" class="required">Booking email</label>
        <input
          v-model.trim="eventModel.bookingEmail"
          id="form-booking-email"
          name="bookingEmail"
          style="width: 100%"
          type="email"
          pattern="^[^(\.)][a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}"
          @focusout="onInputFocusOut"
          :class="eventModelError.bookingEmail && 'invalid'"
          :minlength="formConfig.minLength.bookingName"
          :maxlength="formConfig.maxLength.bookingEmail"
          :disabled="isEditMode || userStore.isAuthenticated"
          required
        />
        <div class="input-validation-box">
          <div class="input-invalid-msg" :style="visibleValidationErrorMsg">
            {{ eventModelError.bookingEmail || '' }}
          </div>
          <div class="input-counter">{{ eventModel.bookingEmail.length }}/{{ formConfig.maxLength.bookingEmail }}</div>
        </div>
      </div>
      <div class="form-group">
        <div class="input-group">
          <label for="form-event-category" class="required">Category</label>
          <select
            v-if="!isEditMode"
            v-model="eventModel.category"
            id="form-event-category"
            name="eventCategory"
            @focusout="onInputFocusOut"
            required
          >
            <option
              v-for="(category, index) in categoryStore.eventCategories"
              :key="category.categoryId"
              :value="category"
            >
              {{ category.categoryName }}
            </option>
          </select>
          <input v-else v-model="eventModel.category.categoryName" disabled />
        </div>
        <div class="input-group">
          <label>Duration (Minutes)</label>
          <input v-if="eventModel.category" v-model="eventModel.category.eventDuration" disabled />
        </div>
        <div class="input-group">
          <label for="form-event-starttime" class="required">Start time</label>
          <input
            v-model="eventModel.eventStartTime"
            id="form-event-starttime"
            name="eventStartTime"
            type="datetime-local"
            @focusout="onInputFocusOut"
            :class="eventModelError.eventStartTime && 'invalid'"
            :min="formConfig.min.eventStartTime"
            required
          />
          <div class="input-validation-box">
            <div class="input-invalid-msg" :style="visibleValidationErrorMsg">
              {{ eventModelError.eventStartTime || '' }}
            </div>
          </div>
        </div>
      </div>
      <div class="input-group">
        <label for="form-event-notes">Notes</label>
        <textarea
          id="form-event-notes"
          :class="eventModelError.eventNotes && 'invalid'"
          v-model.trim="eventModel.eventNotes"
          rows="5"
          :maxlength="formConfig.maxLength.eventNotes"
        ></textarea>
        <div class="input-validation-box">
          <div class="input-invalid-msg" :style="visibleValidationErrorMsg">
            {{ eventModelError.eventNotes || '' }}
          </div>
          <div class="input-counter">
            {{ (eventModel.eventNotes && eventModel.eventNotes.length) || 0 }}/{{ formConfig.maxLength.eventNotes }}
          </div>
        </div>
      </div>
      <div class="upload-field">
        <input ref="fileRef" type="file" name="file" @change="onFileInputChanged" hidden />
        <app-button id="upload-btn" type="button" @click="openFileSelector">
          <font-awesome-icon icon="paperclip" />
        </app-button>
        <div class="file-field">
          {{
            eventModel.file
              ? `${eventModel.file.name} (${(eventModel.file.size / (1024 * 1024)).toFixed(2)} MB)`
              : 'No selected file. (Max: 10MB)'
          }}
          <button v-if="eventModel.file" type="button" class="file-field-remove" @click="removeFile">
            <font-awesome-icon icon="xmark" />
          </button>
        </div>
      </div>
      <div class="input-validation-box">
        <div class="input-invalid-msg" :style="visibleValidationErrorMsg">
          {{ eventModelError.file || '' }}
        </div>
      </div>
      <div class="duration-summary">
        <b>Event period summary : </b>
        <span v-if="!!eventModel.eventStartTime">
          {{ moment(eventModel.eventStartTime).format('LL') }} at
          {{ $getFormattedEventPeriod(eventModel.eventStartTime, eventModel.category.eventDuration) }}
        </span>
        <span v-else>—</span>
      </div>
      <div class="app-button-group">
        <app-button type="submit" button-type="success">{{ isEditMode ? 'Save' : 'Add event' }}</app-button>
        <app-button v-show="!isEditMode" type="reset" button-type="warning">Reset</app-button>
        <app-button type="reset" button-type="danger" @click="modalState.close()">Cancel</app-button>
      </div>
    </form>
  </app-modal>
</template>

<style scoped>
.form-group {
  display: flex;
  justify-content: space-between;
}
.duration-summary {
  margin: 0 10px;
}
.input-group {
  margin: 12px 10px;
}

.input-group label {
  display: block;
  font-size: 10pt;
  margin-right: 6px;
}

.input-validation-box {
  display: flex;
  font-size: 10pt;
  justify-content: space-between;
}

.input-validation-box .input-invalid-msg {
  color: rgb(220, 30, 30);
}

.input-validation-box .input-counter {
  color: rgba(10, 10, 10, 0.5);
}

.upload-field {
  display: flex;
  align-items: center;
  width: fit-content;
  margin: 10px 0;
  width: 100%;
}

.upload-field .file-field {
  position: relative;
  background-color: rgba(135, 206, 250, 0.5);
  padding: 6px 10px;
  font-size: 12px;
  font-style: italic;
  width: 100%;
}

.file-field .file-field-remove {
  position: absolute;
  top: 5px;
  right: 10px;
  outline: none;
  border: none;
  background: transparent;
  color: darkred;
  transition: all 0.3s cubic-bezier(0.445, 0.05, 0.55, 0.95);
}

.file-field .file-field-remove:hover {
  cursor: pointer;
  color: rgb(222, 0, 0);
}

#upload-btn {
  margin: 0;
  padding: 5px;
  font-size: 12px;
  width: 30px;
}
</style>
