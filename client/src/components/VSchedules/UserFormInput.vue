<script setup>
import AppInput from '../../components/App/AppInput.vue'
import { useUserRoles } from '../../stores/userRoles'
import { ref, onBeforeMount } from 'vue'

const roleStore = useUserRoles()

const roleOptions = ref([])

const errorMessage = ref({
  email: '',
  password: '',
  confirmPassword: '',
  name: '',
  role: '',
})

const props = defineProps({
  modalState: {
    type: Object,
  },
  userModel: {
    type: Object,
    default: {},
  },
  userModelError: {
    type: Object,
    default: {},
  },
})

const validatePasswordMatch = () => {
  errorMessage.value.password = ''
  errorMessage.value.confirmPassword = ''

  if (!props.userModel.password || !props.userModel.confirmPassword) {
    return false
  }

  if (props.userModel.password.length < 8 || props.userModel.password.length > 14) {
    errorMessage.value.password = 'Password size must be between 8 and 14.'
    return false
  }

  if (props.userModel.password !== props.userModel.confirmPassword) {
    errorMessage.value.confirmPassword = 'Password mismatch.'
    return false
  } else {
    errorMessage.value.confirmPassword = ''
    return true
  }
}

onBeforeMount(async () => {
  await roleStore.fetchUserRoles()
  roleOptions.value = roleStore.toOptions()
  props.userModel.role = roleOptions.value[0].value
})

defineEmits(['add-user', 'cancel-form'])
</script>

<template>
  <app-modal :show="modalState.state">
    <form @submit.prevent="$emit('add-user')" @reset.prevent="$emit('cancel-form')">
      <h3>Create New Account</h3>
      <div>
        <AppInput
          type="text"
          v-model.trim="userModel.name"
          label-text="Name"
          :maxlength="100"
          :error-message="errorMessage.name"
          :required="true"
        />
        <AppInput
          type="email"
          pattern="^[^(\.)][a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}"
          v-model.trim="userModel.email"
          label-text="Email"
          :maxlength="50"
          :error-message="errorMessage.email"
          :required="true"
        />
        <AppInput
          type="password"
          v-model="userModel.password"
          label-text="Password"
          @focusout="validatePasswordMatch"
          :minlength="8"
          :error-message="errorMessage.password"
          :required="true"
        />
        <AppInput
          type="password"
          v-model="userModel.confirmPassword"
          label-text="Confirm Password"
          @focusout="validatePasswordMatch"
          :minlength="8"
          :error-message="errorMessage.confirmPassword"
          :required="true"
        />
        <AppInput
          v-model.trim="userModel.role"
          label-text="Role"
          :options="roleOptions"
          :error-message="errorMessage.role"
          :required="true"
        />
        <div class="app-button-group">
          <app-button class="btn-submit" button-type="success">Create</app-button>
          <app-button class="btn-cancel" button-type="danger" type="reset" @click="modalState.close()"
            >Cancel</app-button
          >
        </div>
      </div>
    </form>
  </app-modal>
</template>
<style>
.app-button-group {
  display: flex;
}
.btn-cancel,
.btn-submit {
  width: 50%;
}
</style>
