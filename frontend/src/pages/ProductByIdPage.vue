<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <div class="row items-center q-mb-lg">
      <div>
        <div class="text-h5 text-weight-bold text-secondary">Buscar Producto</div>
        <div class="text-caption text-grey-6">Consulta los detalles de un producto por su ID</div>
      </div>
    </div>

    <div class="row q-col-gutter-lg">

      <div class="col-12 col-md-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="bg-primary text-white q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="manage_search" size="20px" />
              <span class="text-subtitle2">Buscar por ID</span>
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

              <q-btn label="Buscar" type="submit" color="primary" icon="search"
                unelevated rounded :loading="store.loading" class="full-width" />

            </q-form>
          </q-card-section>
        </q-card>
      </div>

      <!-- Resultado -->
      <div class="col-12 col-md-8">
        <q-card v-if="product" flat bordered class="rounded-borders">
          <q-card-section class="bg-green-1 q-py-sm q-px-md">
            <div class="row items-center q-gutter-sm">
              <q-icon name="storefront" color="primary" size="20px" />
              <span class="text-subtitle2 text-secondary">Detalles del producto</span>
            </div>
          </q-card-section>

          <q-card-section class="q-pa-lg">
            <div class="row items-center q-mb-lg q-gutter-md">
              <q-avatar color="primary" text-color="white" size="56px">
                <q-icon name="inventory_2" />
              </q-avatar>
              <div>
                <div class="text-h5 text-weight-bold text-secondary">{{ product.name }}</div>
                <q-chip dense color="grey-3" icon="tag">ID {{ product.id }}</q-chip>
              </div>
            </div>

            <div class="row q-col-gutter-md">
              <div class="col-12 col-sm-6">
                <q-card flat class="bg-grey-1 rounded-borders">
                  <q-card-section class="row items-center q-gutter-md q-py-md">
                    <q-icon name="attach_money" color="positive" size="32px" />
                    <div>
                      <div class="text-caption text-grey-6">Precio</div>
                      <div class="text-h6 text-weight-bold text-positive">${{ product.price }}</div>
                    </div>
                  </q-card-section>
                </q-card>
              </div>

              <div v-if="product.description" class="col-12 col-sm-6">
                <q-card flat class="bg-grey-1 rounded-borders">
                  <q-card-section class="row items-center q-gutter-md q-py-md">
                    <q-icon name="notes" color="primary" size="32px" />
                    <div>
                      <div class="text-caption text-grey-6">Descripción</div>
                      <div class="text-body2">{{ product.description }}</div>
                    </div>
                  </q-card-section>
                </q-card>
              </div>
            </div>

            <div class="q-mt-lg row q-gutter-sm">
              <q-btn dense flat rounded label="Ver inventario" icon="analytics"
                color="primary" :to="`/inventory`" />
              <q-btn dense flat rounded label="Realizar compra" icon="shopping_cart"
                color="secondary" :to="`/inventory/purchase`" />
            </div>
          </q-card-section>
        </q-card>

        <q-card v-else flat bordered class="rounded-borders bg-grey-1">
          <q-card-section class="flex flex-center column q-pa-xl q-gutter-md text-grey-4">
            <q-icon name="find_in_page" size="64px" />
            <div class="text-subtitle1">Ingresa un ID para buscar el producto</div>
          </q-card-section>
        </q-card>
      </div>

    </div>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useProductStore } from '../stores/useProductStore'

const store = useProductStore()
const formRef = ref(null)
const productId = ref('')
const product = ref(null)

async function search() {
  const valid = await formRef.value.validate()
  if (!valid) return
  store.clearError()
  product.value = null
  product.value = await store.fetchById(productId.value)
}
</script>
