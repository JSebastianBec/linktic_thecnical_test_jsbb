import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import { makeFormStub } from '../../../test/vitest.setup'
import InventoryPage from '../InventoryPage.vue'
import { useInventoryStore } from '../../stores/useInventoryStore'

function mountPage(formValid = true) {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const wrapper = mount(InventoryPage, {
    global: {
      plugins: [pinia],
      stubs: { QForm: makeFormStub(formValid) },
    },
  })
  return { wrapper, store: useInventoryStore() }
}

describe('InventoryPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('happy path: search llama a fetchByProductId y muestra el inventario', async () => {
    const { wrapper, store } = mountPage()
    store.fetchByProductId.mockResolvedValue({
      id: '1', productId: 1, stock: 42,
      product: { name: 'Laptop', price: 1500 },
    })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(store.fetchByProductId).toHaveBeenCalled()
    expect(wrapper.html()).toContain('Laptop')
  })

  it('edge case: cuando fetchByProductId devuelve null, muestra el estado vacío', async () => {
    const { wrapper, store } = mountPage()
    store.fetchByProductId.mockResolvedValue(null)

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(wrapper.html()).toContain('Ingresa un ID para consultar el stock')
  })

  it('edge case: muestra banner de error cuando store.error está definido', async () => {
    const { wrapper, store } = mountPage()
    store.error = 'Inventario no encontrado'
    await nextTick()

    expect(wrapper.html()).toContain('Inventario no encontrado')
  })
})
