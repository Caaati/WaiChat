import { createI18n } from 'vue-i18n'
import zhCN from '@/locales/zh-CN.json'
import enUS from '@/locales/en-US.json'

export const LOCALE_STORAGE_KEY = 'waichat_locale'

function readStoredLocale() {
  try {
    const s = localStorage.getItem(LOCALE_STORAGE_KEY)
    if (s === 'en-US' || s === 'zh-CN') return s
  } catch (_) {
    /* ignore */
  }
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: true,
  globalInjection: true,
  locale: readStoredLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
  silentTranslationWarn: true,
  silentFallbackWarn: true,
})

export function applyDocumentLang(locale) {
  const short = locale === 'en-US' ? 'en' : 'zh-CN'
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('lang', short)
  }
}

applyDocumentLang(i18n.global.locale)

export default i18n
