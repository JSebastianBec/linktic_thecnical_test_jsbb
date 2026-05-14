<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <div class="row items-center q-mb-lg">
      <div>
        <div class="text-h5 text-weight-bold text-secondary">Actualizar Stock</div>
        <div class="text-caption text-grey-6">Establece la cantidad disponible para un producto</div>
      </div>
    </div>

    <div class="row q-col-gutter-lg">

      <div class="col-12 col-md-6">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="bg-secondary text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="tune" size="20px" />
              <span class="text-subtitle2">Ajuste de inventario</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <q-form ref="formRef" @submit.prevent="submit" class="q-gutter-md">

              <q-input v-model="form.productId" label="ID del producto" outlined
                bg-color="white" color="secondary"
                :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']">
                <template #prepend><q-icon name="tag" color="secondary" /></template>
              </q-input>

              <q-input v-model.number="form.stock" label="Nuevo stock" outlined
                type="number" bg-color="white" color="secondary"
                :rules="[v => v !== null && v !== '' || 'Requerido', v => v >= 0 || 'No puede ser negativo']">
                <template #prepend><q-icon name="layers" color="secondary" /></template>
              </q-input>

              <q-banner v-if="store.error" rounded class="bg-negative text-white">
                <template #avatar><q-icon name="error" /></template>
                {{ store.error }}
              </q-banner>

              <q-btn label="Actualizar Stock" type="submit" color="secondary"
                icon="save" unelevated rounded :loading="store.loading" class="full-width" />

            </q-form>
          </q-card-section>
        </q-card>
      </div>

      <div class="col-12 col-md-6 column q-gutter-md">

        <!-- Resultado -->
        <q-card v-if="result" flat bordered class="rounded-borders">
          <q-card-section class="bg-positive text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="check_circle" size="20px" />
              <span class="text-subtitle2">Stock actualizado</span>
            </div>
          </q-card-section>
          <q-card-section>
            <div class="text-h6 text-weight-bold text-secondary q-mb-sm">
              {{ result.product?.name }}
            </div>
            <div class="row items-center q-gutter-md">
              <div class="column items-center">
                <q-circular-progress :value="100" size="60px" :thickness="0.25"
                  color="positive" track-color="green-2" class="q-mb-xs" />
                <div class="text-h5 text-weight-bold text-positive absolute-center">
                  {{ result.stock }}
                </div>
              </div>
              <div>
                <div class="text-caption text-grey-6">Unidades disponibles</div>
                <q-chip dense :color="result.stock > 0 ? 'positive' : 'negative'"
                  text-color="white" :icon="result.stock > 0 ? 'check' : 'close'">
                  {{ result.stock > 0 ? 'En stock' : 'Sin stock' }}
                </q-chip>
              </div>
            </div>
          </q-card-section>
        </q-card>

        <!-- Info -->
        <q-card flat bordered class="rounded-borders bg-green-1">
          <q-card-section>
            <div class="row items-center q-gutter-sm q-mb-sm">
              <q-icon name="info" color="secondary" size="20px" />
              <span class="text-subtitle2 text-secondary">Nota</span>
            </div>
            <div class="text-caption text-grey-8">
              El valor ingresado reemplaza el stock actual. Si quieres agregar unidades al stock
              existente, primero consulta la cantidad actual en "Consultar stock".
            </div>
          </q-card-section>
        </q-card>

      </div>
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
