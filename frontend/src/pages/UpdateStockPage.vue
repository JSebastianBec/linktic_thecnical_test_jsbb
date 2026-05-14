<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-xl shadow-4">
        <div class="text-h5 text-weight-bold text-primary q-mb-lg">Actualizar Stock</div>

        <q-form ref="formRef" @submit.prevent="submit">
          <q-input v-model="form.productId" label="ID del producto *" outlined class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']" />

          <q-input v-model.number="form.stock" label="Nuevo stock *" outlined
            type="number" class="q-mb-md"
            :rules="[v => v !== null && v !== '' || 'Requerido', v => v >= 0 || 'No puede ser negativo']" />

          <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mb-md">
            {{ store.error }}
          </q-banner>

          <q-btn label="Actualizar" type="submit" color="secondary" icon="update"
            rounded unelevated size="lg" class="full-width" :loading="store.loading" />
        </q-form>

        <q-card v-if="result" flat bordered class="q-mt-lg bg-grey-1">
          <q-card-section>
            <div class="text-positive text-weight-bold">Stock actualizado correctamente</div>
            <div class="q-mt-sm">
              <span class="text-grey-6">Producto:</span> {{ result.product?.name }} —
              <span class="text-grey-6">Nuevo stock:</span>
              <q-chip dense color="positive" text-color="white">{{ result.stock }}</q-chip>
            </div>
          </q-card-section>
        </q-card>
      </q-card>
    </div>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useInventoryStore } from '../stores/useInventoryStore'

const store = useInventoryStore()
const formRef = ref(null)
const result = ref(null)
const form = ref({ productId: '', stock: null })

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return

  store.clearError()
  result.value = null
  result.value = await store.updateStock(form.value.productId, form.value.stock)
}
</script>
