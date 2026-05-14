import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import { makeFormStub } from '../../../test/vitest.setup'
import PurchasePage from '../PurchasePage.vue'
import { useInventoryStore } from '../../stores/useInventoryStore'

const PENDING_PURCHASE = {
  id: 'uuid-1', purchaseId: 'uuid-1',
  productId: 1, quantity: 5,
  status: 'PENDING', attemptCount: 0,
}

function mountPage(formValid = true) {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const wrapper = mount(PurchasePage, {
    global: {
      plugins: [pinia],
      stubs: { QForm: makeFormStub(formValid) },
    },
  })
  return { wrapper, store: useInventoryStore() }
}

describe('PurchasePage', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('happy path: submit llama a requestPurchase y muestra estado PENDING', async () => {
    const { wrapper, store } = mountPage()
    store.requestPurchase.mockResolvedValue(PENDING_PURCHASE)

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(store.requestPurchase).toHaveBeenCalled()
    expect(wrapper.html()).toContain('Procesando')
  })

  it('happy path: el polling actualiza el estado a COMPLETED y se detiene', async () => {
    const { wrapper, store } = mountPage()
    store.requestPurchase.mockResolvedValue(PENDING_PURCHASE)
    store.getPurchaseStatus.mockResolvedValue({ ...PENDING_PURCHASE, status: 'COMPLETED' })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    // Avanzar 3 segundos (un ciclo de polling)
    vi.advanceTimersByTime(3000)
    await nextTick()
    await nextTick()

    expect(store.getPurchaseStatus).toHaveBeenCalledWith('uuid-1')
    expect(wrapper.html()).toContain('Compra exitosa')
  })

  it('happy path: estado FAILED muestra mensaje de rechazo', async () => {
    const { wrapper, store } = mountPage()
    store.requestPurchase.mockResolvedValue(PENDING_PURCHASE)
    store.getPurchaseStatus.mockResolvedValue({ ...PENDING_PURCHASE, status: 'FAILED' })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    vi.advanceTimersByTime(3000)
    await nextTick()
    await nextTick()

    expect(wrapper.html()).toContain('Compra no procesada')
  })

  it('edge case: validación fallida no llama a requestPurchase', async () => {
    const { wrapper, store } = mountPage(false)

    await wrapper.find('form').trigger('submit')
    await nextTick()

    expect(store.requestPurchase).not.toHaveBeenCalled()
  })

  it('edge case: el polling se detiene al desmontar el componente', async () => {
    const { wrapper, store } = mountPage()
    store.requestPurchase.mockResolvedValue(PENDING_PURCHASE)

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    wrapper.unmount()

    vi.advanceTimersByTime(6000) // dos ciclos de polling
    await nextTick()

    // getPurchaseStatus no debe haberse llamado después de desmontar
    expect(store.getPurchaseStatus).not.toHaveBeenCalled()
  })
})
