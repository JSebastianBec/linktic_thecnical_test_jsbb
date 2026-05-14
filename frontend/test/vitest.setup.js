import { config } from '@vue/test-utils'

// Stubs globales para componentes Quasar — evitan errores de renderizado en tests
config.global.stubs = {
  QPage: { template: '<div><slot /></div>' },
  QBtn: {
    template: '<button type="button" @click="$emit(\'click\')"><slot /></button>',
    emits: ['click'],
    props: ['to', 'loading', 'disable', 'label', 'icon', 'type', 'color'],
  },
  QCard: { template: '<div><slot /></div>' },
  QCardSection: { template: '<div><slot /></div>' },
  QCardActions: { template: '<div><slot /></div>' },
  QIcon: { template: '<span />' },
  QChip: { template: '<span><slot /></span>' },
  QBanner: { template: '<div data-testid="q-banner"><slot /></div>' },
  QTable: { template: '<table />' },
  QSpinner: { template: '<div />' },
  QKnob: { template: '<div />' },
  QCircularProgress: { template: '<div />' },
  QLinearProgress: { template: '<div />' },
  QList: { template: '<ul><slot /></ul>' },
  QItem: { template: '<li><slot /></li>' },
  QItemSection: { template: '<span><slot /></span>' },
  QItemLabel: { template: '<span><slot /></span>' },
  QAvatar: { template: '<div><slot /></div>' },
  QSeparator: { template: '<hr />' },
  QScrollArea: { template: '<div><slot /></div>' },
  QInput: {
    // inheritAttrs: false evita que props desconocidas (prefix, bg-color, etc.)
    // se pasen al <input> nativo y generen warnings en jsdom
    inheritAttrs: false,
    template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    props: ['modelValue', 'rules', 'label', 'type', 'color', 'prefix', 'bgColor',
            'outlined', 'standout', 'dark', 'dense', 'placeholder'],
    emits: ['update:modelValue'],
  },
  RouterLink: { template: '<a><slot /></a>' },
  RouterView: { template: '<div />' },
}

// Helper exportado para crear stub de QForm con validate() configurable
export function makeFormStub(validateResult = true) {
  return {
    // Pasa $event (DOM Event) al emitir 'submit' para que el modificador .prevent
    // del padre pueda llamar event.preventDefault() sin error.
    template: '<form @submit.prevent="$emit(\'submit\', $event)"><slot /></form>',
    emits: ['submit'],
    setup() {
      return {
        validate: () => Promise.resolve(validateResult),
        resetValidation: () => {},
      }
    },
  }
}
