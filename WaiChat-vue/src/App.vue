<script setup>
import { computed } from 'vue'
import { RouterView } from 'vue-router'
import i18n, { LOCALE_STORAGE_KEY, applyDocumentLang } from '@/i18n'

const currentLocale = computed({
  get() {
    return i18n.global.locale
  },
  set(v) {
    i18n.global.locale = v
    try {
      localStorage.setItem(LOCALE_STORAGE_KEY, v)
    } catch (_) {
      /* ignore */
    }
    applyDocumentLang(v)
  },
})

function onLocaleChange(e) {
  currentLocale.value = e.target.value
}
</script>

<template>
  <div class="app-shell">
    <div class="app-locale-bar" role="region" :aria-label="$t('app.uiLanguage')">
      <label class="locale-label" for="app-locale-select">{{ $t('app.uiLanguage') }}</label>
      <select
        id="app-locale-select"
        class="locale-select wc-input"
        :value="currentLocale"
        @change="onLocaleChange"
      >
        <option value="zh-CN">{{ $t('app.langZh') }}</option>
        <option value="en-US">{{ $t('app.langEn') }}</option>
      </select>
    </div>
    <div class="router-fill">
      <RouterView />
    </div>
  </div>
</template>

<style scoped>
.app-shell {
  height: 100%;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.app-locale-bar {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding: 6px 12px;
  border-bottom: 1px solid var(--wc-border, #e2e8f0);
  background: color-mix(in srgb, var(--wc-bg, #f8fafc) 92%, transparent);
  z-index: 50;
}

.locale-label {
  font-size: 12px;
  color: var(--wc-text-secondary, #64748b);
  font-weight: 600;
}

.locale-select {
  width: auto;
  min-width: 120px;
  padding: 4px 8px;
  font-size: 13px;
}

.router-fill {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.router-fill > :deep(*) {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}
</style>
