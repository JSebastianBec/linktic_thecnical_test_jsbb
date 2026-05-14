import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import { nextTick } from 'vue'
import { makeFormStub } from '../../../test/vitest.setup'
import ProductsPage from '../ProductsPage.vue'
import { useProductStore } from '../../stores/useProductStore'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ path: '/products' }),
}))

vi.mock('quasar', () => ({
  useQuasar: () => ({ notify: vi.fn() }),
}))

function mountPage(formValid = true) {
  const pinia = createTestingPinia({ createSpy: vi.fn })
  const wrapper = mount(ProductsPage, {
    global: {
      plugins: [pinia],
      stubs: { QForm: makeFormStub(formValid) },
    },
  })
  return { wrapper, store: useProductStore() }
}

describe('ProductsPage', () => {
  beforeEach(() => vi.clearAllMocks())

  it('happy path: muestra el formulario al cargar la página', () => {
    const { wrapper } = mountPage()
    expect(wrapper.find('form').exists()).toBe(true)
  })

  it('happy path: submit llama a store.create y muestra el producto creado', async () => {
    const { wrapper, store } = mountPage()
    store.create.mockResolvedValue({ id: '1', name: 'Laptop', price: 1500 })

    await wrapper.find('form').trigger('submit')
    await nextTick()
    await nextTick()

    expect(store.create).toHaveBeenCalled()
    expect(wrapper.html()).toContain('Laptop')
  })

  it('edge case: muestra el banner de error cuando store.error está definido', async () => {
    const { wrapper, store } = mountPage()
    store.error = 'No se pudo crear el producto'
    await nextTick()

    expect(wrapper.html()).toContain('No se pudo crear el producto')
  })

  it('edge case: validación fallida no llama a store.create', async () => {
    const { wrapper, store } = mountPage(false)

    await wrapper.find('form').trigger('submit')
    await nextTick()

    expect(store.create).not.toHaveBeenCalled()
  })
})
