<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-xl shadow-4">
        <div class="text-h5 text-weight-bold text-primary q-mb-lg">Realizar Compra</div>

        <q-form ref="formRef" @submit.prevent="submit" v-if="!purchase">
          <q-input v-model="form.productId" label="ID del producto *" outlined class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']" />

          <q-input v-model.number="form.quantity" label="Cantidad *" outlined
            type="number" class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => v > 0 || 'Debe ser mayor a 0']" />

          <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mb-md">
            {{ store.error }}
          </q-banner>

          <q-btn label="Solicitar Compra" type="submit" color="positive" icon="shopping_cart"
            rounded unelevated size="lg" class="full-width" :loading="store.loading" />
        </q-form>

        <!-- Estado de la compra -->
        <div v-if="purchase" class="q-mt-md">
          <div class="text-subtitle1 q-mb-md">
            Solicitud <span class="text-weight-bold">{{ purchase.purchaseId?.slice(0, 8) }}...</span>
          </div>

          <!-- PENDING -->
          <q-card v-if="purchase.status === 'PENDING'" flat bordered class="bg-blue-1">
            <q-card-section class="row items-center q-gutter-md">
              <q-spinner color="primary" size="32px" />
              <div>
                <div class="text-weight-bold text-primary">Procesando compra...</div>
                <div class="text-caption text-grey-6">
                  Intento {{ purchase.attemptCount }} de 3 — verificando stock disponible
                </div>
              </div>
            </q-card-section>
          </q-card>

          <!-- COMPLETED -->
          <q-card v-else-if="purchase.status === 'COMPLETED'" flat bordered class="bg-positive text-white">
            <q-card-section>
              <div class="text-h6"><q-icon name="check_circle" /> Compra realizada</div>
              <div class="q-mt-sm text-caption">
                Producto ID: {{ purchase.productId }} — Cantidad: {{ purchase.quantity }}
              </div>
            </q-card-section>
          </q-card>

          <!-- FAILED -->
          <q-card v-else-if="purchase.status === 'FAILED'" flat bordered class="bg-negative text-white">
            <q-card-section>
              <div class="text-h6"><q-icon name="cancel" /> Compra fallida</div>
              <div class="q-mt-sm text-caption">{{ purchase.failureReason }}</div>
            </q-card-section>
          </q-card>

          <q-btn flat label="Nueva compra" color="primary" class="q-mt-md" @click="reset" />
        </div>
      </q-card>
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

async function submit() {
  const valid = await formRef.value.validate()
  if (!valid) return

  store.clearError()
  const result = await store.requestPurchase(
    Number(form.value.productId),
    form.value.quantity
  )

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
  if (pollingInterval) {
    clearInterval(pollingInterval)
    pollingInterval = null
  }
}

function reset() {
  stopPolling()
  purchase.value = null
  form.value = { productId: '', quantity: null }
  store.clearError()
}

onUnmounted(() => stopPolling())
</script>
