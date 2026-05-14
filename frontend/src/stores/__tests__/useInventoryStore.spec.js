import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useInventoryStore } from '../useInventoryStore'
import { inventoryApi } from '../../services/api'

vi.mock('../../services/api', () => ({
  inventoryApi: { get: vi.fn(), post: vi.fn(), put: vi.fn() },
  extractApiError: vi.fn((err) => err?.response?.data?.errors?.[0]?.detail ?? 'Error'),
}))

const INVENTORY = { id: '1', productId: 1, stock: 50, product: { name: 'Laptop', price: 1500 } }
const PURCHASE = {
  id: 'uuid-1', purchaseId: 'uuid-1', productId: 1,
  quantity: 5, status: 'PENDING', attemptCount: 0,
}

const toApiItem = (d) => ({
  data: { data: { id: String(d.id ?? d.productId), attributes: { ...d } } },
})
const toApiList = (items) => ({
  data: { data: items.map((d) => ({ id: String(d.id ?? d.productId), attributes: { ...d } })) },
})

describe('useInventoryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  // ── fetchAll ──────────────────────────────────────────────────────────────

  it('fetchAll — happy path: maps inventory list correctly', async () => {
    inventoryApi.get.mockResolvedValue(toApiList([INVENTORY]))
    const store = useInventoryStore()

    await store.fetchAll()

    expect(store.inventoryList).toHaveLength(1)
    expect(store.inventoryList[0].stock).toBe(50)
    expect(store.loading).toBe(false)
  })

  it('fetchAll — edge case: sets error on failure', async () => {
    inventoryApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'Server error' }] } } })
    const store = useInventoryStore()

    await store.fetchAll()

    expect(store.inventoryList).toHaveLength(0)
    expect(store.error).toBe('Server error')
  })

  // ── fetchByProductId ──────────────────────────────────────────────────────

  it('fetchByProductId — happy path: returns inventory for product', async () => {
    inventoryApi.get.mockResolvedValue(toApiItem(INVENTORY))
    const store = useInventoryStore()

    const result = await store.fetchByProductId(1)

    expect(result.stock).toBe(50)
    expect(inventoryApi.get).toHaveBeenCalledWith('/1')
  })

  it('fetchByProductId — edge case: returns null when product not found', async () => {
    inventoryApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'No encontrado' }] } } })
    const store = useInventoryStore()

    const result = await store.fetchByProductId(999)

    expect(result).toBeNull()
    expect(store.error).toBe('No encontrado')
  })

  // ── updateStock ───────────────────────────────────────────────────────────

  it('updateStock — happy path: sends new stock and returns updated inventory', async () => {
    inventoryApi.put.mockResolvedValue(toApiItem({ ...INVENTORY, stock: 100 }))
    const store = useInventoryStore()

    const result = await store.updateStock(1, 100)

    expect(result.stock).toBe(100)
    expect(inventoryApi.put).toHaveBeenCalledWith('/1/stock', { stock: 100 })
  })

  it('updateStock — edge case: returns null and sets error when product not found', async () => {
    inventoryApi.put.mockRejectedValue({ response: { data: { errors: [{ detail: 'Producto inexistente' }] } } })
    const store = useInventoryStore()

    const result = await store.updateStock(999, 10)

    expect(result).toBeNull()
    expect(store.error).toBe('Producto inexistente')
  })

  // ── requestPurchase ───────────────────────────────────────────────────────

  it('requestPurchase — happy path: returns purchase with PENDING status', async () => {
    inventoryApi.post.mockResolvedValue(toApiItem(PURCHASE))
    const store = useInventoryStore()

    const result = await store.requestPurchase(1, 5)

    expect(result.status).toBe('PENDING')
    expect(inventoryApi.post).toHaveBeenCalledWith('/purchases', { productId: 1, quantity: 5 })
  })

  it('requestPurchase — edge case: returns null when request fails', async () => {
    inventoryApi.post.mockRejectedValue({ response: { data: { errors: [{ detail: 'Sin stock' }] } } })
    const store = useInventoryStore()

    const result = await store.requestPurchase(1, 999)

    expect(result).toBeNull()
    expect(store.error).toBe('Sin stock')
  })

  // ── getPurchaseStatus ─────────────────────────────────────────────────────

  it('getPurchaseStatus — happy path: returns current status of purchase', async () => {
    inventoryApi.get.mockResolvedValue(toApiItem({ ...PURCHASE, status: 'COMPLETED' }))
    const store = useInventoryStore()

    const result = await store.getPurchaseStatus('uuid-1')

    expect(result.status).toBe('COMPLETED')
  })

  it('getPurchaseStatus — edge case: returns null when purchase not found', async () => {
    inventoryApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'No encontrado' }] } } })
    const store = useInventoryStore()

    const result = await store.getPurchaseStatus('bad-uuid')

    expect(result).toBeNull()
  })

  // ── clearError ────────────────────────────────────────────────────────────

  it('clearError — resets error to null', async () => {
    inventoryApi.get.mockRejectedValue({ response: { data: { errors: [{ detail: 'Error' }] } } })
    const store = useInventoryStore()
    await store.fetchAll()

    store.clearError()

    expect(store.error).toBeNull()
  })
})
