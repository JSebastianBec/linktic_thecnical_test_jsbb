<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <div class="row items-center q-mb-lg">
      <div>
        <div class="text-h5 text-weight-bold text-secondary">Consultar Stock</div>
        <div class="text-caption text-grey-6">Ingresa el ID de un producto para ver su inventario</div>
      </div>
    </div>

    <div class="row q-col-gutter-lg">

      <div class="col-12 col-md-5">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="bg-primary text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="search" size="20px" />
              <span class="text-subtitle2">Buscar inventario</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <q-form ref="formRef" @submit.prevent="search" class="q-gutter-md">

              <q-input v-model="productId" label="ID del producto" outlined
                bg-color="white" color="primary"
                :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']">
                <template #prepend><q-icon name="tag" color="primary" /></template>
              </q-input>

              <q-banner v-if="store.error" rounded class="bg-negative text-white">
                <template #avatar><q-icon name="error" /></template>
                {{ store.error }}
              </q-banner>

              <q-btn label="Consultar" type="submit" color="primary" icon="search"
                unelevated rounded :loading="store.loading" class="full-width" />

            </q-form>
          </q-card-section>
        </q-card>
      </div>

      <!-- Resultado -->
      <div class="col-12 col-md-7">
        <q-card v-if="inventory" flat bordered class="rounded-borders">
          <q-card-section class="bg-green-1 q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="analytics" color="primary" size="20px" />
              <span class="text-subtitle2 text-secondary">Resultado</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <div class="row items-start q-col-gutter-lg">

              <!-- Stock visual -->
              <div class="col-12 col-sm-5 flex flex-center column q-gutter-sm">
                <q-knob :model-value="Math.min(inventory.stock, 100)" readonly
                  size="120px" :thickness="0.2" color="primary" track-color="grey-3"
                  show-value font-size="20px" class="text-secondary text-weight-bold" />
                <div class="text-caption text-grey-6">unidades disponibles</div>
                <q-chip :color="inventory.stock > 0 ? 'positive' : 'negative'"
                  text-color="white" :icon="inventory.stock > 0 ? 'check' : 'close'" dense>
                  {{ inventory.stock > 0 ? 'En stock' : 'Agotado' }}
                </q-chip>
              </div>

              <!-- Datos del producto -->
              <div class="col-12 col-sm-7">
                <div class="text-h6 text-weight-bold text-secondary q-mb-md">
                  {{ inventory.product?.name }}
                </div>
                <q-list dense>
                  <q-item>
                    <q-item-section avatar>
                      <q-icon name="tag" color="primary" size="18px" />
                    </q-item-section>
                    <q-item-section>
                      <q-item-label caption>Producto ID</q-item-label>
                      <q-item-label>{{ inventory.productId }}</q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item>
                    <q-item-section avatar>
                      <q-icon name="attach_money" color="primary" size="18px" />
                    </q-item-section>
                    <q-item-section>
                      <q-item-label caption>Precio</q-item-label>
                      <q-item-label>${{ inventory.product?.price }}</q-item-label>
                    </q-item-section>
                  </q-item>
                  <q-item>
                    <q-item-section avatar>
                      <q-icon name="inventory_2" color="primary" size="18px" />
                    </q-item-section>
                    <q-item-section>
                      <q-item-label caption>Stock actual</q-item-label>
                      <q-item-label class="text-h6 text-weight-bold">{{ inventory.stock }}</q-item-label>
                    </q-item-section>
                  </q-item>
                </q-list>

                <div class="q-mt-md row q-gutter-sm">
                  <q-btn dense flat rounded label="Actualizar" icon="tune"
                    color="secondary" :to="`/inventory/stock`" />
                  <q-btn dense flat rounded label="Comprar" icon="shopping_cart"
                    color="primary" :to="`/inventory/purchase`" />
                </div>
              </div>

            </div>
          </q-card-section>
        </q-card>

        <!-- Estado vacío -->
        <q-card v-else flat bordered class="rounded-borders bg-grey-1">
          <q-card-section class="flex flex-center column q-pa-xl q-gutter-md text-grey-4">
            <q-icon name="search_off" size="64px" />
            <div class="text-subtitle1">Ingresa un ID para consultar el stock</div>
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
const productId = ref('')
const inventory = ref(null)

async function search() {
  const valid = await formRef.value.validate()
  if (!valid) return
  store.clearError()
  inventory.value = null
  inventory.value = await store.fetchByProductId(productId.value)
}
</script>
