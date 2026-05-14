<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <div class="row items-center q-mb-lg">
      <div>
        <div class="text-h5 text-weight-bold text-secondary">Realizar Compra</div>
        <div class="text-caption text-grey-6">La solicitud se procesa de forma asíncrona</div>
      </div>
    </div>

    <div class="row q-col-gutter-lg">

      <!-- Formulario -->
      <div class="col-12 col-md-6">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="bg-primary text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="shopping_cart" size="20px" />
              <span class="text-subtitle2">Nueva solicitud</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <q-form ref="formRef" @submit.prevent="submit" class="q-gutter-md" v-if="!purchase">

              <q-input v-model="form.productId" label="ID del producto" outlined
                bg-color="white" color="primary"
                :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']">
                <template #prepend><q-icon name="tag" color="primary" /></template>
              </q-input>

              <q-input v-model.number="form.quantity" label="Cantidad" outlined
                type="number" bg-color="white" color="primary"
                :rules="[v => !!v || 'Requerido', v => v > 0 || 'Debe ser mayor a 0']">
                <template #prepend><q-icon name="production_quantity_limits" color="primary" /></template>
              </q-input>

              <q-banner v-if="store.error" rounded class="bg-negative text-white">
                <template #avatar><q-icon name="error" /></template>
                {{ store.error }}
              </q-banner>

              <q-btn label="Solicitar Compra" type="submit" color="primary"
                icon="shopping_cart_checkout" unelevated rounded
                :loading="store.loading" class="full-width" />
            </q-form>

            <!-- Estado de la compra -->
            <div v-if="purchase">

              <div class="text-caption text-grey-6 q-mb-md">
                ID: <span class="text-weight-bold text-secondary">{{ purchase.purchaseId?.slice(0, 8) }}...</span>
              </div>

              <!-- PENDING -->
              <div v-if="purchase.status === 'PENDING'">
                <div class="flex flex-center column q-pa-lg q-gutter-md">
                  <q-circular-progress indeterminate size="64px" color="primary" track-color="grey-3" />
                  <div class="text-center">
                    <div class="text-subtitle1 text-weight-bold text-primary">Procesando...</div>
                    <div class="text-caption text-grey-6">
                      Intento {{ purchase.attemptCount || 1 }} de 3
                    </div>
                    <div class="text-caption text-grey-5 q-mt-xs">
                      Verificando stock disponible
                    </div>
                  </div>
                  <q-linear-progress rounded indeterminate color="primary"
                    track-color="green-2" class="full-width" style="height: 6px" />
                </div>
              </div>

              <!-- COMPLETED -->
              <div v-else-if="purchase.status === 'COMPLETED'">
                <div class="flex flex-center column q-pa-lg q-gutter-md">
                  <q-icon name="check_circle" color="positive" size="64px" />
                  <div class="text-center">
                    <div class="text-subtitle1 text-weight-bold text-positive">¡Compra exitosa!</div>
                    <div class="text-caption text-grey-6 q-mt-xs">
                      Producto ID: {{ purchase.productId }} · Cantidad: {{ purchase.quantity }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- FAILED -->
              <div v-else-if="purchase.status === 'FAILED'">
                <div class="flex flex-center column q-pa-lg q-gutter-md">
                  <q-icon name="cancel" color="negative" size="64px" />
                  <div class="text-center">
                    <div class="text-subtitle1 text-weight-bold text-negative">Compra no procesada</div>
                    <div class="text-caption text-grey-6 q-mt-xs">No hay unidades suficientes disponibles en este momento.</div>
                  </div>
                </div>
              </div>

              <q-btn flat rounded label="Nueva compra" icon="refresh"
                color="primary" class="full-width q-mt-sm" @click="reset" />
            </div>
          </q-card-section>
        </q-card>
      </div>

      <!-- Panel informativo -->
      <div class="col-12 col-md-6 column q-gutter-md">

        <q-card flat bordered class="rounded-borders bg-green-1">
          <q-card-section>
            <div class="row items-center q-gutter-sm q-mb-sm">
              <q-icon name="tips_and_updates" color="primary" size="20px" />
              <span class="text-subtitle2 text-secondary">¿Cómo funciona?</span>
            </div>
            <q-list dense>
              <q-item v-for="tip in tips" :key="tip.text" class="q-pa-xs">
                <q-item-section avatar>
                  <q-icon :name="tip.icon" color="primary" size="18px" />
                </q-item-section>
                <q-item-section class="text-caption text-grey-8">{{ tip.text }}</q-item-section>
              </q-item>
            </q-list>
          </q-card-section>
        </q-card>

        <q-card v-if="purchase?.status === 'PENDING'" flat bordered class="rounded-borders">
          <q-card-section>
            <div class="row items-center q-gutter-sm q-mb-sm">
              <q-icon name="hourglass_top" color="primary" size="20px" />
              <span class="text-subtitle2 text-secondary">Verificando disponibilidad</span>
            </div>
            <div class="row q-gutter-sm q-mb-sm">
              <q-chip v-for="n in 3" :key="n"
                :color="(purchase?.attemptCount || 0) >= n ? 'primary' : 'grey-3'"
                :text-color="(purchase?.attemptCount || 0) >= n ? 'white' : 'grey-7'"
                dense icon="refresh">
                {{ n }}/3
              </q-chip>
            </div>
            <div class="text-caption text-grey-6">
              El sistema está verificando si hay unidades disponibles.
            </div>
          </q-card-section>
        </q-card>

      </div>
    </div>
  </q-page>
</template>

<script setup>
import { ref, onUnmounted } from 'vue'
import { useInventoryStore } from '../stores/useInventoryStore'

const store = useInventoryStore()
const formRef = ref(null)
const form = ref({ productId: '', quantity: null })
const purchase = ref(null)
let pollingInterval = null

const tips = [
  { icon: 'tag',                text: 'Ingresa el ID del producto que deseas comprar.' },
  { icon: 'production_quantity_limits', text: 'La cantidad debe ser mayor a cero.' },
  { icon: 'hourglass_top',      text: 'La solicitud se procesa en segundos. Espera el resultado.' },
  { icon: 'inventory_2',        text: 'Si no hay suficientes unidades, la compra será rechazada.' },
]

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return
  store.clearError()
  const result = await store.requestPurchase(Number(form.value.productId), form.value.quantity)
  if (result) {
    purchase.value = result
    if (result.status === 'PENDING') startPolling(result.purchaseId)
  }
}

function startPolling(purchaseId) {
  pollingInterval = setInterval(async () => {
    const status = await store.getPurchaseStatus(purchaseId)
    if (status) {
      purchase.value = status
      if (status.status !== 'PENDING') stopPolling()
    }
  }, 3000)
}

function stopPolling() {
  clearInterval(pollingInterval)
  pollingInterval = null
}

function reset() {
  stopPolling()
  purchase.value = null
  form.value = { productId: '', quantity: null }
  store.clearError()
}

onUnmounted(() => stopPolling())
</script>
