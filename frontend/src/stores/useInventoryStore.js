import { defineStore } from 'pinia'
import { ref } from 'vue'
import { inventoryApi, extractApiError } from '../services/api'

export const useInventoryStore = defineStore('inventory', () => {
  const inventoryList = ref([])
  const loading = ref(false)
  const error = ref(null)

  const clearError = () => (error.value = null)

  async function fetchAll() {
    loading.value = true
    error.value = null
    try {
      const res = await inventoryApi.get('')
      inventoryList.value = res.data.data.map((item) => ({
        id: item.id,
        ...item.attributes,
      }))
    } catch (err) {
      error.value = extractApiError(err)
    } finally {
      loading.value = false
    }
  }

  async function fetchByProductId(productId) {
    loading.value = true
    error.value = null
    try {
      const res = await inventoryApi.get(`/${productId}`)
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateStock(productId, stock) {
    loading.value = true
    error.value = null
    try {
      const res = await inventoryApi.put(`/${productId}/stock`, { stock })
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function requestPurchase(productId, quantity) {
    loading.value = true
    error.value = null
    try {
      const res = await inventoryApi.post('/purchases', { productId, quantity })
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    } finally {
      loading.value = false
    }
  }

  async function getPurchaseStatus(purchaseId) {
    try {
      const res = await inventoryApi.get(`/purchases/${purchaseId}`)
      return { id: res.data.data.id, ...res.data.data.attributes }
    } catch (err) {
      error.value = extractApiError(err)
      return null
    }
  }

  return {
    inventoryList, loading, error, clearError,
    fetchAll, fetchByProductId, updateStock, requestPurchase, getPurchaseStatus,
  }
})
