<template>
  <div class="term-page wc-page">
    <div class="term-inner wc-glass-card">
      <header class="term-head">
        <button type="button" class="wc-btn wc-btn-ghost back-btn" @click="goChat">{{ $t('terminology.backChat') }}</button>
        <h1>{{ $t('terminology.title') }}</h1>
        <p class="sub">
          {{ $t('terminology.intro') }}
        </p>
      </header>

      <div class="tabs">
        <button type="button" :class="['tab', { active: tab === 'mine' }]" @click="tab = 'mine'">{{ $t('terminology.tabMine') }}</button>
        <button type="button" :class="['tab', { active: tab === 'system' }]" @click="tab = 'system'">{{ $t('terminology.tabSystem') }}</button>
      </div>

      <div v-if="tab === 'mine'" class="panel">
        <div class="toolbar">
          <button type="button" class="wc-btn wc-btn-primary" @click="openCreate">{{ $t('terminology.addTerm') }}</button>
        </div>
        <p v-if="loadError" class="err">{{ loadError }}</p>
        <template v-else>
          <section v-for="g in mineGrouped.groups" :key="'mg' + g.groupId" class="group-block">
            <button
              type="button"
              class="group-title-btn"
              :aria-expanded="mineGroupOpen(g.groupId)"
              @click="toggleMineGroup(g.groupId)"
            >
              <span class="group-chev" :class="{ open: mineGroupOpen(g.groupId) }" aria-hidden="true">▶</span>
              <span class="group-title-text">{{ g.groupName }}</span>
              <span class="group-meta">{{ (g.entries || []).length }} {{ $t('terminology.entries') }}</span>
            </button>
            <div v-show="mineGroupOpen(g.groupId)" class="group-body">
              <div class="table-wrap">
              <table class="term-table term-table-mine">
                <colgroup>
                  <col class="col-term" />
                  <col class="col-alias" />
                  <col class="col-def" />
                  <col class="col-weight" />
                  <col class="col-enabled" />
                  <col class="col-src" />
                  <col class="col-actions" />
                </colgroup>
                <thead>
                  <tr>
                    <th>{{ $t('terminology.thTerm') }}</th>
                    <th>{{ $t('terminology.thAlias') }}</th>
                    <th>{{ $t('terminology.thDef') }}</th>
                    <th>{{ $t('terminology.thWeight') }}</th>
                    <th>{{ $t('terminology.thEnabled') }}</th>
                    <th>{{ $t('terminology.thSource') }}</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in g.entries" :key="row.id">
                    <td class="cell-term">{{ row.term }}</td>
                    <td class="cell-alias">{{ formatAliasColumn(row) }}</td>
                    <td class="def cell-def">{{ row.definition }}</td>
                    <td class="cell-weight">{{ row.sortWeight }}</td>
                    <td class="cell-enabled">
                      <button
                        type="button"
                        class="enabled-toggle"
                        :class="{ 'is-on': row.enabled === 1 }"
                        :disabled="togglingTermId === row.id"
                        @click="toggleRowEnabled(row)"
                      >
                        {{ row.enabled === 1 ? $t('terminology.enabledOn') : $t('terminology.enabledOff') }}
                      </button>
                    </td>
                    <td class="cell-src">
                      <span v-if="row.clonedFromSystemId" class="badge-sys">{{ $t('terminology.badgeSysClone') }}</span>
                      <span v-else class="badge-own">{{ $t('terminology.badgeOwn') }}</span>
                    </td>
                    <td class="actions">
                      <button type="button" class="linkish" @click="openEdit(row)">{{ $t('terminology.edit') }}</button>
                      <button type="button" class="linkish danger" @click="remove(row)">{{ $t('terminology.delete') }}</button>
                    </td>
                  </tr>
                </tbody>
              </table>
              </div>
            </div>
          </section>

          <section v-if="mineGrouped.ungrouped && mineGrouped.ungrouped.length" class="group-block">
            <h3 class="group-title">{{ $t('terminology.ungroupedTitle') }}</h3>
            <div class="table-wrap">
              <table class="term-table term-table-mine">
                <colgroup>
                  <col class="col-term" />
                  <col class="col-alias" />
                  <col class="col-def" />
                  <col class="col-weight" />
                  <col class="col-enabled" />
                  <col class="col-src" />
                  <col class="col-actions" />
                </colgroup>
                <thead>
                  <tr>
                    <th>{{ $t('terminology.thTerm') }}</th>
                    <th>{{ $t('terminology.thAlias') }}</th>
                    <th>{{ $t('terminology.thDef') }}</th>
                    <th>{{ $t('terminology.thWeight') }}</th>
                    <th>{{ $t('terminology.thEnabled') }}</th>
                    <th>{{ $t('terminology.thSource') }}</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in mineGrouped.ungrouped" :key="row.id">
                    <td class="cell-term">{{ row.term }}</td>
                    <td class="cell-alias">{{ formatAliasColumn(row) }}</td>
                    <td class="def cell-def">{{ row.definition }}</td>
                    <td class="cell-weight">{{ row.sortWeight }}</td>
                    <td class="cell-enabled">
                      <button
                        type="button"
                        class="enabled-toggle"
                        :class="{ 'is-on': row.enabled === 1 }"
                        :disabled="togglingTermId === row.id"
                        @click="toggleRowEnabled(row)"
                      >
                        {{ row.enabled === 1 ? $t('terminology.enabledOn') : $t('terminology.enabledOff') }}
                      </button>
                    </td>
                    <td class="cell-src">
                      <span v-if="row.clonedFromSystemId" class="badge-sys">{{ $t('terminology.badgeSysClone') }}</span>
                      <span v-else class="badge-own">{{ $t('terminology.badgeOwn') }}</span>
                    </td>
                    <td class="actions">
                      <button type="button" class="linkish" @click="openEdit(row)">{{ $t('terminology.edit') }}</button>
                      <button type="button" class="linkish danger" @click="remove(row)">{{ $t('terminology.delete') }}</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <p v-if="mineTotallyEmpty && !loading" class="empty">{{ $t('terminology.emptyMine') }}</p>
        </template>
      </div>

      <div v-else class="panel">
        <div class="toolbar system-toolbar">
          <button
            type="button"
            class="wc-btn wc-btn-primary"
            :disabled="importing || selectableImportCount === 0"
            @click="importSelectedToMine"
          >
            {{ importing ? $t('terminology.importing') : $t('terminology.importBtn', { n: selectableImportCount }) }}
          </button>
          <span class="hint">{{ $t('terminology.hintSystem') }}</span>
        </div>
        <p v-if="loadError" class="err">{{ loadError }}</p>
        <template v-else>
          <section v-if="systemTree.ungrouped && systemTree.ungrouped.length" class="group-block">
            <h3 class="group-title">{{ $t('terminology.ungroupedSys') }}</h3>
            <div class="table-wrap">
              <table class="term-table term-table-system">
                <colgroup>
                  <col class="col-chk" />
                  <col class="col-term-sys" />
                  <col class="col-alias-sys" />
                  <col class="col-def-sys" />
                  <col class="col-status-sys" />
                </colgroup>
                <thead>
                  <tr>
                    <th class="col-chk">{{ $t('terminology.thSelect') }}</th>
                    <th>{{ $t('terminology.thTerm') }}</th>
                    <th>{{ $t('terminology.thAlias') }}</th>
                    <th>{{ $t('terminology.thDef') }}</th>
                    <th>{{ $t('terminology.thStatus') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in systemTree.ungrouped"
                    :key="'u' + row.id"
                    :class="{ 'row-imported': isSystemImported(row.id) }"
                  >
                    <td class="col-chk">
                      <input
                        type="checkbox"
                        :checked="isSystemRowSelected(row.id)"
                        :disabled="isSystemImported(row.id)"
                        @change="onSystemCheckChange(row.id, $event.target.checked)"
                      />
                    </td>
                    <td class="cell-term">{{ row.term }}</td>
                    <td class="cell-alias">{{ formatAliasColumn(row) }}</td>
                    <td class="def cell-def">{{ row.definition }}</td>
                    <td class="cell-status">
                      <span v-if="isSystemImported(row.id)" class="badge-in">{{ $t('terminology.statusImported') }}</span>
                      <span v-else class="muted">{{ $t('terminology.statusOptional') }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-for="grp in systemTree.groups" :key="'sg' + grp.groupId" class="group-block">
            <button
              type="button"
              class="group-title-btn"
              :aria-expanded="systemGroupOpen(grp.groupId)"
              @click="toggleSystemGroup(grp.groupId)"
            >
              <span class="group-chev" :class="{ open: systemGroupOpen(grp.groupId) }" aria-hidden="true">▶</span>
              <span class="group-title-text">{{ grp.groupName }}</span>
              <span class="group-meta">{{ (grp.entries || []).length }} {{ $t('terminology.entries') }}</span>
            </button>
            <div v-show="systemGroupOpen(grp.groupId)" class="group-body">
              <div class="group-head">
                <div class="group-actions">
                  <button type="button" class="wc-btn wc-btn-ghost btn-sm" @click="selectAllInGroup(grp)">{{ $t('terminology.selectAll') }}</button>
                  <button type="button" class="wc-btn wc-btn-ghost btn-sm" @click="clearGroupSelection(grp)">{{ $t('terminology.clearGroup') }}</button>
                  <button
                    type="button"
                    class="wc-btn wc-btn-primary btn-sm"
                    :disabled="importing"
                    @click="importGroupAll(grp.groupId)"
                  >
                    {{ $t('terminology.importGroup') }}
                  </button>
                </div>
              </div>
              <div class="table-wrap">
              <table class="term-table term-table-system">
                <colgroup>
                  <col class="col-chk" />
                  <col class="col-term-sys" />
                  <col class="col-alias-sys" />
                  <col class="col-def-sys" />
                  <col class="col-status-sys" />
                </colgroup>
                <thead>
                  <tr>
                    <th class="col-chk">{{ $t('terminology.thSelect') }}</th>
                    <th>{{ $t('terminology.thTerm') }}</th>
                    <th>{{ $t('terminology.thAlias') }}</th>
                    <th>{{ $t('terminology.thDef') }}</th>
                    <th>{{ $t('terminology.thStatus') }}</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="row in grp.entries"
                    :key="'g' + row.id"
                    :class="{ 'row-imported': isSystemImported(row.id) }"
                  >
                    <td class="col-chk">
                      <input
                        type="checkbox"
                        :checked="isSystemRowSelected(row.id)"
                        :disabled="isSystemImported(row.id)"
                        @change="onSystemCheckChange(row.id, $event.target.checked)"
                      />
                    </td>
                    <td class="cell-term">{{ row.term }}</td>
                    <td class="cell-alias">{{ formatAliasColumn(row) }}</td>
                    <td class="def cell-def">{{ row.definition }}</td>
                    <td class="cell-status">
                      <span v-if="isSystemImported(row.id)" class="badge-in">{{ $t('terminology.statusImported') }}</span>
                      <span v-else class="muted">{{ $t('terminology.statusOptional') }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
              </div>
            </div>
          </section>

          <p v-if="systemTreeEmpty && !loading" class="empty">{{ $t('terminology.emptySystem') }}</p>
        </template>
      </div>

      <div v-if="editor.open" class="editor-overlay" @click.self="closeEditor">
        <div class="editor wc-glass-card" @click.stop>
          <h3>{{ editor.id ? $t('terminology.editorEdit') : $t('terminology.editorCreate') }}</h3>
          <label>{{ $t('terminology.labelTerm') }}</label>
          <input v-model="editor.term" class="wc-input" maxlength="128" />
          <label>{{ $t('terminology.labelDef') }}</label>
          <textarea v-model="editor.definition" class="wc-input area" rows="4"></textarea>
          <label>{{ $t('terminology.labelAliasOptional') }}</label>
          <p class="field-hint">{{ $t('terminology.aliasHint') }}</p>
          <div class="alias-editor">
            <div v-for="(ar, idx) in editor.aliasRows" :key="'ar' + idx" class="alias-editor-row">
              <input v-model="ar.alias" class="wc-input alias-text" maxlength="128" :placeholder="$t('terminology.aliasPlaceholder')" type="text" />
              <select v-model="ar.targetLang" class="wc-input alias-lang" :title="$t('terminology.aliasLangTitle')">
                <option value="">{{ $t('terminology.aliasAny') }}</option>
                <option v-for="lang in languages" :key="lang.code" :value="lang.code">
                  {{ lang.flag }} {{ lang.name }}
                </option>
              </select>
              <button type="button" class="wc-btn wc-btn-ghost btn-sm" @click="removeAliasRow(idx)">{{ $t('terminology.removeAlias') }}</button>
            </div>
            <button type="button" class="wc-btn wc-btn-ghost add-alias-btn" @click="addAliasRow">{{ $t('terminology.addAlias') }}</button>
          </div>
          <div class="row2">
            <div>
              <label>{{ $t('terminology.labelSort') }}</label>
              <input v-model.number="editor.sortWeight" type="number" class="wc-input" />
            </div>
            <div>
              <label>{{ $t('terminology.labelEnabled') }}</label>
              <select v-model.number="editor.enabled" class="wc-input">
                <option :value="1">{{ $t('terminology.yes') }}</option>
                <option :value="0">{{ $t('terminology.no') }}</option>
              </select>
            </div>
          </div>
          <p v-if="editor.error" class="err">{{ editor.error }}</p>
          <div class="editor-actions">
            <button type="button" class="wc-btn wc-btn-ghost" @click="closeEditor">{{ $t('terminology.cancel') }}</button>
            <button type="button" class="wc-btn wc-btn-primary" :disabled="saving" @click="saveEditor">
              {{ saving ? $t('terminology.saving') : $t('terminology.save') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { CODES } from '@/constants/codes.js';
import * as terminologyApi from '@/api/terminology.js';

export default {
  name: 'TerminologyView',
  data() {
    return {
      tab: 'mine',
      mineGrouped: { ungrouped: [], groups: [] },
      systemTree: { ungrouped: [], groups: [] },
      languages: [],
      loading: false,
      loadError: '',
      saving: false,
      editor: {
        open: false,
        id: null,
        term: '',
        definition: '',
        aliasRows: [],
        sortWeight: 0,
        enabled: 1,
        error: ''
      },
      selectedSystemIds: [],
      importing: false,
      togglingTermId: null,
      /** 术语组是否展开：key 为 groupId，未出现或为 false 表示默认折叠 */
      expandedMineGroups: {},
      expandedSystemGroups: {}
    };
  },
  computed: {
    selectableImportCount() {
      return this.selectedSystemIds.filter((id) => !this.isSystemImported(id)).length;
    },
    mineTotallyEmpty() {
      const m = this.mineGrouped;
      return (
        (!m.groups || m.groups.length === 0) &&
        (!m.ungrouped || m.ungrouped.length === 0)
      );
    },
    systemTreeEmpty() {
      const t = this.systemTree;
      return (!t.groups || t.groups.length === 0) && (!t.ungrouped || t.ungrouped.length === 0);
    }
  },
  mounted() {
    this.refreshAll();
    this.loadLanguages();
  },
  methods: {
    async loadLanguages() {
      try {
        const res = await axios.get('/api/ai/languages');
        if (res.data.code === CODES.SUCCESS && Array.isArray(res.data.data)) {
          this.languages = res.data.data;
        }
      } catch (_) {
        /* 未登录或网络错误时术语页仍可编辑，仅无语言下拉 */
      }
    },
    storedChatTargetLang() {
      try {
        return localStorage.getItem('waichatTargetLang') || '';
      } catch (_) {
        return '';
      }
    },
    defaultAliasTargetLang() {
      const stored = this.storedChatTargetLang();
      if (stored && this.languages.some((l) => l.code === stored)) {
        return stored;
      }
      return this.languages[0]?.code || '';
    },
    addAliasRow() {
      this.editor.aliasRows.push({
        alias: '',
        targetLang: this.defaultAliasTargetLang()
      });
    },
    removeAliasRow(idx) {
      this.editor.aliasRows.splice(idx, 1);
    },
    formatOneAlias(a) {
      if (a == null) return '';
      if (typeof a === 'string') {
        const s = a.trim();
        return s || '';
      }
      const name = (a.alias || '').trim();
      if (!name) return '';
      const lang = (a.targetLang || '').trim();
      return lang ? `${name}（${lang}）` : name;
    },
    formatAliasColumn(row) {
      const list = row.aliases || [];
      if (!list.length) return '—';
      return list.map((x) => this.formatOneAlias(x)).filter(Boolean).join('；') || '—';
    },
    normalizeAliasesForApi(aliases) {
      if (!aliases || !aliases.length) return [];
      return aliases
        .map((a) => {
          if (typeof a === 'string') {
            const alias = a.trim();
            return alias ? { alias, targetLang: null } : null;
          }
          const alias = (a.alias || '').trim();
          const tl = (a.targetLang || '').trim();
          return alias ? { alias, targetLang: tl || null } : null;
        })
        .filter(Boolean);
    },
    mineGroupOpen(groupId) {
      return this.expandedMineGroups[groupId] === true;
    },
    toggleMineGroup(groupId) {
      this.expandedMineGroups = {
        ...this.expandedMineGroups,
        [groupId]: !this.mineGroupOpen(groupId)
      };
    },
    systemGroupOpen(groupId) {
      return this.expandedSystemGroups[groupId] === true;
    },
    toggleSystemGroup(groupId) {
      this.expandedSystemGroups = {
        ...this.expandedSystemGroups,
        [groupId]: !this.systemGroupOpen(groupId)
      };
    },
    getMineFlat() {
      const m = this.mineGrouped || {};
      const out = [...(m.ungrouped || [])];
      for (const g of m.groups || []) {
        for (const e of g.entries || []) {
          out.push(e);
        }
      }
      return out;
    },
    isSystemImported(systemId) {
      return this.getMineFlat().some((r) => r.clonedFromSystemId === systemId);
    },
    isSystemRowSelected(id) {
      return this.selectedSystemIds.includes(id);
    },
    onSystemCheckChange(id, checked) {
      if (this.isSystemImported(id)) return;
      const i = this.selectedSystemIds.indexOf(id);
      if (checked && i < 0) {
        this.selectedSystemIds.push(id);
      } else if (!checked && i >= 0) {
        this.selectedSystemIds.splice(i, 1);
      }
    },
    selectAllInGroup(grp) {
      for (const e of grp.entries || []) {
        if (this.isSystemImported(e.id)) continue;
        if (!this.selectedSystemIds.includes(e.id)) {
          this.selectedSystemIds.push(e.id);
        }
      }
    },
    clearGroupSelection(grp) {
      for (const e of grp.entries || []) {
        const i = this.selectedSystemIds.indexOf(e.id);
        if (i >= 0) this.selectedSystemIds.splice(i, 1);
      }
    },
    async importGroupAll(groupId) {
      this.importing = true;
      try {
        const res = await terminologyApi.importSystemTermsToMine({
          systemTerminologyIds: [],
          systemGroupIds: [groupId]
        });
        if (res.data.code === CODES.SUCCESS) {
          const list = res.data.data || [];
          this.selectedSystemIds = [];
          await this.refreshAll();
          alert(list.length ? this.$t('terminology.importAdded', { n: list.length }) : this.$t('terminology.importNoneGroup'));
        } else {
          alert(res.data.message || this.$t('terminology.importFail'));
        }
      } catch (e) {
        alert(this.$t('terminology.importFail'));
      } finally {
        this.importing = false;
      }
    },
    async importSelectedToMine() {
      const pending = this.selectedSystemIds.filter((id) => !this.isSystemImported(id));
      if (!pending.length) {
        alert(this.$t('terminology.selectFirst'));
        return;
      }
      this.importing = true;
      try {
        const res = await terminologyApi.importSystemTermsToMine({
          systemTerminologyIds: pending,
          systemGroupIds: []
        });
        if (res.data.code === CODES.SUCCESS) {
          const list = res.data.data || [];
          this.selectedSystemIds = [];
          await this.refreshAll();
          alert(list.length ? this.$t('terminology.importAdded', { n: list.length }) : this.$t('terminology.importNoneSelected'));
        } else {
          alert(res.data.message || this.$t('terminology.importFail'));
        }
      } catch (e) {
        alert(this.$t('terminology.importFail'));
      } finally {
        this.importing = false;
      }
    },
    goChat() {
      this.$router.push('/chat');
    },
    async refreshAll() {
      this.loading = true;
      this.loadError = '';
      try {
        const [mineRes, sysRes] = await Promise.all([
          terminologyApi.fetchMineGrouped(),
          terminologyApi.fetchSystemGroups()
        ]);
        if (mineRes.data.code === CODES.SUCCESS) {
          this.mineGrouped = mineRes.data.data || { ungrouped: [], groups: [] };
        } else {
          this.loadError = mineRes.data.message || this.$t('terminology.loadMineFail');
        }
        if (sysRes.data.code === CODES.SUCCESS) {
          this.systemTree = sysRes.data.data || { ungrouped: [], groups: [] };
        } else if (!this.loadError) {
          this.loadError = sysRes.data.message || this.$t('terminology.loadSysFail');
        }
      } catch (e) {
        this.loadError = this.$t('terminology.loadNetwork');
      } finally {
        this.loading = false;
      }
    },
    openCreate() {
      this.editor = {
        open: true,
        id: null,
        term: '',
        definition: '',
        aliasRows: [],
        sortWeight: 0,
        enabled: 1,
        error: ''
      };
    },
    openEdit(row) {
      const raw = (row.aliases || []).map((a) =>
        typeof a === 'string'
          ? { alias: a, targetLang: '' }
          : { alias: a.alias || '', targetLang: a.targetLang || '' }
      );
      this.editor = {
        open: true,
        id: row.id,
        term: row.term,
        definition: row.definition,
        aliasRows: raw.length ? raw : [],
        sortWeight: row.sortWeight != null ? row.sortWeight : 0,
        enabled: row.enabled != null ? row.enabled : 1,
        error: ''
      };
    },
    closeEditor() {
      this.editor.open = false;
    },
    async saveEditor() {
      this.editor.error = '';
      if (!this.editor.term.trim() || !this.editor.definition.trim()) {
        this.editor.error = this.$t('terminology.errTermDef');
        return;
      }
      const payload = {
        term: this.editor.term.trim(),
        definition: this.editor.definition.trim(),
        aliases: this.normalizeAliasesForApi(this.editor.aliasRows),
        sortWeight: this.editor.sortWeight,
        enabled: this.editor.enabled
      };
      this.saving = true;
      try {
        let res;
        if (this.editor.id) {
          res = await terminologyApi.updateMyTerm(this.editor.id, payload);
        } else {
          res = await terminologyApi.createMyTerm(payload);
        }
        if (res.data.code === CODES.SUCCESS) {
          this.closeEditor();
          await this.refreshAll();
        } else {
          this.editor.error = res.data.message || this.$t('terminology.saveFail');
        }
      } catch (e) {
        this.editor.error = this.$t('terminology.saveNetwork');
      } finally {
        this.saving = false;
      }
    },
    async remove(row) {
      if (!confirm(this.$t('terminology.confirmDelete', { term: row.term }))) return;
      try {
        const res = await terminologyApi.deleteMyTerm(row.id);
        if (res.data.code === CODES.SUCCESS) {
          await this.refreshAll();
        } else {
          alert(res.data.message || this.$t('terminology.deleteFail'));
        }
      } catch (e) {
        alert(this.$t('terminology.deleteFail'));
      }
    },
    async toggleRowEnabled(row) {
      if (this.togglingTermId !== null) return;
      const next = row.enabled === 1 ? 0 : 1;
      this.togglingTermId = row.id;
      try {
        const res = await terminologyApi.updateMyTerm(row.id, {
          term: row.term,
          definition: row.definition,
          aliases: this.normalizeAliasesForApi(row.aliases),
          sortWeight: row.sortWeight,
          enabled: next
        });
        if (res.data.code === CODES.SUCCESS) {
          row.enabled = next;
        } else {
          alert(res.data.message || this.$t('terminology.updateFail'));
        }
      } catch (e) {
        alert(this.$t('terminology.networkErr'));
      } finally {
        this.togglingTermId = null;
      }
    }
  }
};
</script>

<style scoped>
.term-page {
  min-height: 100%;
  padding: 20px;
  box-sizing: border-box;
  overflow: auto;
}

.term-inner {
  max-width: 960px;
  margin: 0 auto;
  padding: 22px 24px 28px;
}

.term-head {
  margin-bottom: 18px;
}

.term-head h1 {
  margin: 10px 0 6px;
  font-size: 22px;
}

.sub {
  margin: 0;
  font-size: 13px;
  color: var(--wc-text-secondary, #64748b);
}

.back-btn {
  margin-bottom: 4px;
}

.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tab {
  padding: 8px 14px;
  border-radius: 999px;
  border: 1px solid var(--wc-border, #e2e8f0);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
}

.tab.active {
  background: var(--wc-primary, #6366f1);
  color: #fff;
  border-color: transparent;
}

.toolbar {
  margin-bottom: 12px;
}

.system-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.system-toolbar .hint {
  font-size: 12px;
  color: var(--wc-text-secondary, #64748b);
}

.group-block {
  margin-bottom: 22px;
}

.group-title-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  margin: 0;
  padding: 10px 12px;
  border: 1px solid var(--wc-border, #e2e8f0);
  border-radius: 10px;
  background: color-mix(in srgb, var(--wc-primary, #6366f1) 6%, transparent);
  cursor: pointer;
  text-align: left;
  box-sizing: border-box;
  font: inherit;
}

.group-title-btn:hover {
  background: color-mix(in srgb, var(--wc-primary, #6366f1) 11%, transparent);
}

.group-chev {
  display: inline-block;
  flex-shrink: 0;
  width: 1em;
  font-size: 10px;
  line-height: 1;
  color: var(--wc-text-secondary, #64748b);
  transition: transform 0.16s ease;
  transform: rotate(0deg);
}

.group-chev.open {
  transform: rotate(90deg);
}

.group-title-text {
  flex: 1;
  min-width: 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--wc-text, #0f172a);
}

.group-meta {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--wc-text-secondary, #64748b);
}

.group-body {
  margin-top: 10px;
}

.group-title {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 700;
  color: var(--wc-text, #0f172a);
}

.group-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: flex-start;
  gap: 10px;
  margin-bottom: 10px;
}

.group-head .group-title {
  margin: 0;
}

.group-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.col-chk {
  width: 52px;
  text-align: center;
}

.row-imported {
  opacity: 0.85;
}

.badge-sys,
.badge-own,
.badge-in {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  font-weight: 600;
}

.badge-sys {
  background: color-mix(in srgb, var(--wc-primary, #6366f1) 18%, transparent);
  color: var(--wc-primary, #6366f1);
}

.badge-own {
  background: var(--wc-border-soft, #e2e8f0);
  color: var(--wc-text-secondary, #64748b);
}

.badge-in {
  background: #ecfdf5;
  color: #047857;
}

.muted {
  font-size: 12px;
  color: var(--wc-text-secondary, #94a3b8);
}

.table-wrap {
  overflow-x: auto;
}

.term-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.term-table-mine,
.term-table-system {
  table-layout: fixed;
}

.term-table-mine col.col-term {
  width: 11%;
}
.term-table-mine col.col-alias {
  width: 17%;
}
.term-table-mine col.col-def {
  width: 30%;
}
.term-table-mine col.col-weight {
  width: 7%;
}
.term-table-mine col.col-enabled {
  width: 84px;
}
.term-table-mine col.col-src {
  width: 84px;
}
.term-table-mine col.col-actions {
  width: 92px;
}

.term-table-system col.col-chk {
  width: 52px;
}
.term-table-system col.col-term-sys {
  width: 14%;
}
.term-table-system col.col-alias-sys {
  width: 18%;
}
.term-table-system col.col-def-sys {
  width: 38%;
}
.term-table-system col.col-status-sys {
  width: 14%;
}

.term-table th,
.term-table td {
  border-bottom: 1px solid var(--wc-border, #e2e8f0);
  padding: 10px 8px;
  text-align: left;
  vertical-align: top;
}

.term-table th {
  font-weight: 600;
  color: var(--wc-text-secondary, #64748b);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 所有术语表：表头垂直居中 */
.term-table-mine thead th,
.term-table-system thead th {
  vertical-align: middle;
}

/* 我的术语：权重 / 启用 / 来源 / 操作列表头居中 */
.term-table-mine thead th:nth-child(4),
.term-table-mine thead th:nth-child(5),
.term-table-mine thead th:nth-child(6),
.term-table-mine thead th:nth-child(7) {
  text-align: center;
}

/* 我的术语：正文行垂直居中（与多行释义同高时对齐一致） */
.term-table-mine tbody td {
  vertical-align: middle;
}

.term-table-mine tbody td.cell-weight,
.term-table-mine tbody td.cell-enabled,
.term-table-mine tbody td.cell-src,
.term-table-mine tbody td.actions {
  text-align: center;
}

.cell-term,
.cell-weight {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.term-table-mine tbody td.cell-src {
  white-space: normal;
}

.cell-alias {
  font-weight: 600;
  color: #6d28d9;
  background: linear-gradient(180deg, rgba(124, 58, 237, 0.14), rgba(124, 58, 237, 0.05));
  border: 1px solid rgba(109, 40, 217, 0.35);
  border-radius: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.cell-enabled {
  text-align: center;
}

.enabled-toggle {
  display: inline-block;
  padding: 5px 12px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f1f5f9;
  cursor: pointer;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.25;
  color: #64748b;
  min-width: 56px;
  vertical-align: middle;
}

.enabled-toggle.is-on {
  background: #ecfdf5;
  border-color: #6ee7b7;
  color: #047857;
}

.enabled-toggle:not(.is-on) {
  background: #fef2f2;
  border-color: #fecaca;
  color: #b91c1c;
}

.enabled-toggle:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.def {
  white-space: pre-wrap;
  word-break: break-word;
}

.term-table-mine .cell-def,
.term-table-system .cell-def {
  max-width: none;
  max-height: 5.5em;
  overflow: auto;
}

.actions {
  white-space: nowrap;
  text-align: center;
}

/* 系统默认表：正文垂直居中；选择列与状态列水平居中 */
.term-table-system tbody td {
  vertical-align: middle;
}

.term-table-system thead th.col-chk,
.term-table-system thead th:nth-child(5),
.term-table-system tbody td.col-chk,
.term-table-system tbody td.cell-status {
  text-align: center;
}

.linkish {
  border: none;
  background: none;
  color: var(--wc-primary, #6366f1);
  cursor: pointer;
  margin-right: 8px;
  font-size: 13px;
}

.linkish.danger {
  color: #dc2626;
}

.empty {
  color: var(--wc-text-secondary, #64748b);
  font-size: 14px;
  margin-top: 12px;
}

.err {
  color: #dc2626;
  font-size: 13px;
}

.editor-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 16px;
}

.editor {
  width: min(560px, 100%);
  padding: 20px 22px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.editor h3 {
  margin: 0 0 6px;
}

.editor label {
  font-size: 12px;
  font-weight: 600;
  color: var(--wc-text-secondary, #64748b);
  margin-top: 6px;
}

.area {
  resize: vertical;
  min-height: 72px;
}

.field-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--wc-text-secondary, #64748b);
  line-height: 1.45;
}

.alias-editor {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.alias-editor-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.alias-editor-row .alias-text {
  flex: 1 1 140px;
  min-width: 0;
}

.alias-editor-row .alias-lang {
  flex: 0 1 200px;
  min-width: 140px;
}

.add-alias-btn {
  align-self: flex-start;
}

.row2 {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.editor-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
}
</style>
