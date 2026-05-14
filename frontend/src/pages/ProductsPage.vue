<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-xl shadow-4">
        <div class="text-h5 text-weight-bold text-primary q-mb-lg">Crear Producto</div>

        <q-form ref="formRef" @submit.prevent="submit">
          <q-input v-model="form.name" label="Nombre *" outlined class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/.test(v) || 'Solo letras']" />

          <q-input v-model.number="form.price" label="Precio *" outlined type="number"
            prefix="$" class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => v > 0 || 'Debe ser positivo']" />

          <q-input v-model="form.description" label="Descripción" outlined class="q-mb-md" />

          <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mb-md">
            {{ store.error }}
          </q-banner>

          <q-btn label="Crear Producto" type="submit" color="primary" icon="add"
            rounded unelevated size="lg" class="full-width" :loading="store.loading" />
        </q-form>
      </q-card>
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

const form = ref({ name: '', price: null, description: '' })

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return

  store.clearError()
  const product = await store.create(form.value)

  if (product) {
    $q.notify({ type: 'positive', message: `Producto creado con ID: ${product.id}` })
    form.value = { name: '', price: null, description: '' }
    formRef.value.resetValidation()
    setTimeout(() => router.push('/products/list'), 800)
  }
}
</script>
