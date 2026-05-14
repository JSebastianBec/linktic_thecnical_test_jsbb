<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <div class="row items-center q-mb-lg">
      <div>
        <div class="text-h5 text-weight-bold text-secondary">Nuevo Producto</div>
        <div class="text-caption text-grey-6">Completa los campos para registrar un producto</div>
      </div>
    </div>

    <div class="row q-col-gutter-lg">

      <!-- Formulario -->
      <div class="col-12 col-md-7">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="bg-primary text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="edit_note" size="20px" />
              <span class="text-subtitle2">Información del producto</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <q-form ref="formRef" @submit.prevent="submit" class="q-gutter-md">

              <q-input v-model="form.name" label="Nombre del producto" outlined
                bg-color="white" color="primary"
                :rules="[v => !!v || 'Requerido', v => /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(v) || 'Solo letras']">
                <template #prepend><q-icon name="label" color="primary" /></template>
              </q-input>

              <q-input v-model.number="form.price" label="Precio" outlined
                type="number" bg-color="white" color="primary" prefix="$"
                :rules="[v => !!v || 'Requerido', v => v > 0 || 'Debe ser positivo']">
                <template #prepend><q-icon name="attach_money" color="primary" /></template>
              </q-input>

              <q-input v-model="form.description" label="Descripción (opcional)"
                outlined bg-color="white" color="primary" type="textarea" rows="3">
                <template #prepend><q-icon name="notes" color="primary" /></template>
              </q-input>

              <q-banner v-if="store.error" rounded class="bg-negative text-white">
                <template #avatar><q-icon name="error" /></template>
                {{ store.error }}
              </q-banner>

              <div class="row q-gutter-sm q-pt-sm">
                <q-btn label="Crear Producto" type="submit" color="primary"
                  icon="add_circle" unelevated rounded :loading="store.loading" class="col" />
                <q-btn label="Limpiar" flat color="grey-7" icon="clear_all"
                  @click="reset" :disable="store.loading" />
              </div>

            </q-form>
          </q-card-section>
        </q-card>
      </div>

      <!-- Panel lateral -->
      <div class="col-12 col-md-5 column q-gutter-md">

        <q-card flat bordered class="rounded-borders bg-green-1">
          <q-card-section>
            <div class="row items-center q-gutter-sm q-mb-sm">
              <q-icon name="tips_and_updates" color="primary" size="20px" />
              <span class="text-subtitle2 text-secondary">Recuerda</span>
            </div>
            <q-list dense>
              <q-item v-for="tip in tips" :key="tip" class="q-pa-xs">
                <q-item-section avatar>
                  <q-icon name="check_circle" color="primary" size="16px" />
                </q-item-section>
                <q-item-section class="text-caption text-grey-8">{{ tip }}</q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>

        <q-card v-if="lastCreated" flat bordered class="rounded-borders">
          <q-card-section class="bg-positive text-white">
            <div class="row items-center q-gutter-sm">
              <q-icon name="check_circle" size="20px" />
              <span class="text-subtitle2">Último creado</span>
            </div>
          </q-card-section>
          <q-card-section>
            <div class="text-h6 text-weight-bold text-secondary">{{ lastCreated.name }}</div>
            <div class="row q-gutter-md q-mt-xs">
              <q-chip dense icon="tag" color="grey-3">ID: {{ lastCreated.id }}</q-chip>
              <q-chip dense icon="attach_money" color="green-2">{{ lastCreated.price }}</q-chip>
            </div>
          </q-card-section>
        </q-card>

      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useQuasar } from 'quasar'
import { useProductStore } from '../stores/useProductStore'

const $q = useQuasar()
const router = useRouter()
const store = useProductStore()
const formRef = ref(null)
const lastCreated = ref(null)

const form = ref({ name: '', price: null, description: '' })

const tips = [
  'El nombre solo puede contener letras.',
  'El precio debe ser un valor positivo.',
  'La descripción es opcional.',
  'Al crear el producto, quedará disponible en el inventario automáticamente.',
]

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return
  store.clearError()
  const product = await store.create(form.value)
  if (product) {
    lastCreated.value = product
    $q.notify({ type: 'positive', message: `Producto "${product.name}" creado — ID: ${product.id}` })
    reset()
    setTimeout(() => router.push('/products/list'), 1200)
  }
}

function reset() {
  form.value = { name: '', price: null, description: '' }
  formRef.value?.resetValidation()
}
</script>
