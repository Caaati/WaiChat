<template>
  <div class="term-page wc-page">
    <div class="term-inner wc-glass-card">
      <header class="term-head">
        <button type="button" class="wc-btn wc-btn-ghost back-btn" @click="goChat">返回聊天</button>
        <h1>术语库</h1>
        <p class="sub">
          自定义术语会在润色、翻译、智能回复、总结与分析等 AI 功能中按关键词匹配。系统默认支持「术语组」（如医疗行业），可勾选组内词条或一键导入整组；「我的术语」按组展示。
        </p>
      </header>

      <div class="tabs">
        <button type="button" :class="['tab', { active: tab === 'mine' }]" @click="tab = 'mine'">我的术语</button>
        <button type="button" :class="['tab', { active: tab === 'system' }]" @click="tab = 'system'">系统默认（只读）</button>
      </div>

      <div v-if="tab === 'mine'" class="panel">
        <div class="toolbar">
          <button type="button" class="wc-btn wc-btn-primary" @click="openCreate">新增术语</button>
        </div>
        <p v-if="loadError" class="err">{{ loadError }}</p>
        <template v-else>
          <section v-for="g in mineGrouped.groups" :key="'mg' + g.groupId" class="group-block">
            <h3 class="group-title">{{ g.groupName }}</h3>
            <div class="table-wrap">
              <table class="term-table">
                <thead>
                  <tr>
                    <th>术语</th>
                    <th>释义</th>
                    <th>别名</th>
                    <th>权重</th>
                    <th>启用</th>
                    <th>来源</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in g.entries" :key="row.id">
                    <td>{{ row.term }}</td>
                    <td class="def">{{ row.definition }}</td>
                    <td>{{ (row.aliases || []).join('、') }}</td>
                    <td>{{ row.sortWeight }}</td>
                    <td>{{ row.enabled === 1 ? '是' : '否' }}</td>
                    <td>
                      <span v-if="row.clonedFromSystemId" class="badge-sys">系统副本</span>
                      <span v-else class="badge-own">自填</span>
                    </td>
                    <td class="actions">
                      <button type="button" class="linkish" @click="openEdit(row)">编辑</button>
                      <button type="button" class="linkish danger" @click="remove(row)">删除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-if="mineGrouped.ungrouped && mineGrouped.ungrouped.length" class="group-block">
            <h3 class="group-title">自定义 / 未分组</h3>
            <div class="table-wrap">
              <table class="term-table">
                <thead>
                  <tr>
                    <th>术语</th>
                    <th>释义</th>
                    <th>别名</th>
                    <th>权重</th>
                    <th>启用</th>
                    <th>来源</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="row in mineGrouped.ungrouped" :key="row.id">
                    <td>{{ row.term }}</td>
                    <td class="def">{{ row.definition }}</td>
                    <td>{{ (row.aliases || []).join('、') }}</td>
                    <td>{{ row.sortWeight }}</td>
                    <td>{{ row.enabled === 1 ? '是' : '否' }}</td>
                    <td>
                      <span v-if="row.clonedFromSystemId" class="badge-sys">系统副本</span>
                      <span v-else class="badge-own">自填</span>
                    </td>
                    <td class="actions">
                      <button type="button" class="linkish" @click="openEdit(row)">编辑</button>
                      <button type="button" class="linkish danger" @click="remove(row)">删除</button>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <p v-if="mineTotallyEmpty && !loading" class="empty">暂无术语，可从「系统默认」导入或点击「新增术语」。</p>
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
            {{ importing ? '处理中…' : `将选中词条加入我的术语（${selectableImportCount}）` }}
          </button>
          <span class="hint">可按组勾选词条，或使用组内「加入本组全部」。已导入的词条不可再选。</span>
        </div>
        <p v-if="loadError" class="err">{{ loadError }}</p>
        <template v-else>
          <section v-if="systemTree.ungrouped && systemTree.ungrouped.length" class="group-block">
            <h3 class="group-title">无组词条</h3>
            <div class="table-wrap">
              <table class="term-table">
                <thead>
                  <tr>
                    <th class="col-chk">选择</th>
                    <th>术语</th>
                    <th>释义</th>
                    <th>别名</th>
                    <th>状态</th>
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
                    <td>{{ row.term }}</td>
                    <td class="def">{{ row.definition }}</td>
                    <td>{{ (row.aliases || []).join('、') }}</td>
                    <td>
                      <span v-if="isSystemImported(row.id)" class="badge-in">已在我的术语</span>
                      <span v-else class="muted">可选</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-for="grp in systemTree.groups" :key="'sg' + grp.groupId" class="group-block">
            <div class="group-head">
              <h3 class="group-title">{{ grp.groupName }}</h3>
              <div class="group-actions">
                <button type="button" class="wc-btn wc-btn-ghost btn-sm" @click="selectAllInGroup(grp)">全选本组</button>
                <button type="button" class="wc-btn wc-btn-ghost btn-sm" @click="clearGroupSelection(grp)">取消本组</button>
                <button
                  type="button"
                  class="wc-btn wc-btn-primary btn-sm"
                  :disabled="importing"
                  @click="importGroupAll(grp.groupId)"
                >
                  加入本组全部
                </button>
              </div>
            </div>
            <div class="table-wrap">
              <table class="term-table">
                <thead>
                  <tr>
                    <th class="col-chk">选择</th>
                    <th>术语</th>
                    <th>释义</th>
                    <th>别名</th>
                    <th>状态</th>
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
                    <td>{{ row.term }}</td>
                    <td class="def">{{ row.definition }}</td>
                    <td>{{ (row.aliases || []).join('、') }}</td>
                    <td>
                      <span v-if="isSystemImported(row.id)" class="badge-in">已在我的术语</span>
                      <span v-else class="muted">可选</span>
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <p v-if="systemTreeEmpty && !loading" class="empty">暂无系统术语。</p>
        </template>
      </div>

      <div v-if="editor.open" class="editor-overlay" @click.self="closeEditor">
        <div class="editor wc-glass-card" @click.stop>
          <h3>{{ editor.id ? '编辑术语' : '新增术语' }}</h3>
          <label>标准术语</label>
          <input v-model="editor.term" class="wc-input" maxlength="128" />
          <label>释义</label>
          <textarea v-model="editor.definition" class="wc-input area" rows="4"></textarea>
          <label>别名（每行一个，可选）</label>
          <textarea v-model="editor.aliasesText" class="wc-input area" rows="3" placeholder="缩写或常用说法，每行一条"></textarea>
          <div class="row2">
            <div>
              <label>排序权重</label>
              <input v-model.number="editor.sortWeight" type="number" class="wc-input" />
            </div>
            <div>
              <label>启用</label>
              <select v-model.number="editor.enabled" class="wc-input">
                <option :value="1">是</option>
                <option :value="0">否</option>
              </select>
            </div>
          </div>
          <p v-if="editor.error" class="err">{{ editor.error }}</p>
          <div class="editor-actions">
            <button type="button" class="wc-btn wc-btn-ghost" @click="closeEditor">取消</button>
            <button type="button" class="wc-btn wc-btn-primary" :disabled="saving" @click="saveEditor">
              {{ saving ? '保存中…' : '保存' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { CODES } from '@/constants/codes.js';
import * as terminologyApi from '@/api/terminology.js';

export default {
  name: 'TerminologyView',
  data() {
    return {
      tab: 'mine',
      mineGrouped: { ungrouped: [], groups: [] },
      systemTree: { ungrouped: [], groups: [] },
      loading: false,
      loadError: '',
      saving: false,
      editor: {
        open: false,
        id: null,
        term: '',
        definition: '',
        aliasesText: '',
        sortWeight: 0,
        enabled: 1,
        error: ''
      },
      selectedSystemIds: [],
      importing: false
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
  },
  methods: {
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
          alert(list.length ? `已加入 ${list.length} 条到我的术语` : '组内词条均已存在或无效，未新增');
        } else {
          alert(res.data.message || '导入失败');
        }
      } catch (e) {
        alert('导入失败');
      } finally {
        this.importing = false;
      }
    },
    async importSelectedToMine() {
      const pending = this.selectedSystemIds.filter((id) => !this.isSystemImported(id));
      if (!pending.length) {
        alert('请先勾选尚未加入「我的术语」的系统词条');
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
          alert(list.length ? `已加入 ${list.length} 条到我的术语` : '所选均已存在或无效，未新增');
        } else {
          alert(res.data.message || '导入失败');
        }
      } catch (e) {
        alert('导入失败');
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
          this.loadError = mineRes.data.message || '加载我的术语失败';
        }
        if (sysRes.data.code === CODES.SUCCESS) {
          this.systemTree = sysRes.data.data || { ungrouped: [], groups: [] };
        } else if (!this.loadError) {
          this.loadError = sysRes.data.message || '加载系统术语失败';
        }
      } catch (e) {
        this.loadError = '网络错误或未登录，请先登录后再打开术语库。';
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
        aliasesText: '',
        sortWeight: 0,
        enabled: 1,
        error: ''
      };
    },
    openEdit(row) {
      this.editor = {
        open: true,
        id: row.id,
        term: row.term,
        definition: row.definition,
        aliasesText: (row.aliases || []).join('\n'),
        sortWeight: row.sortWeight != null ? row.sortWeight : 0,
        enabled: row.enabled != null ? row.enabled : 1,
        error: ''
      };
    },
    closeEditor() {
      this.editor.open = false;
    },
    aliasesFromText(text) {
      if (!text || !text.trim()) return [];
      return text
        .split(/\r?\n/)
        .map((s) => s.trim())
        .filter(Boolean);
    },
    async saveEditor() {
      this.editor.error = '';
      if (!this.editor.term.trim() || !this.editor.definition.trim()) {
        this.editor.error = '请填写术语与释义';
        return;
      }
      const payload = {
        term: this.editor.term.trim(),
        definition: this.editor.definition.trim(),
        aliases: this.aliasesFromText(this.editor.aliasesText),
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
          this.editor.error = res.data.message || '保存失败';
        }
      } catch (e) {
        this.editor.error = '网络或服务器错误';
      } finally {
        this.saving = false;
      }
    },
    async remove(row) {
      if (!confirm(`确定删除「${row.term}」？`)) return;
      try {
        const res = await terminologyApi.deleteMyTerm(row.id);
        if (res.data.code === CODES.SUCCESS) {
          await this.refreshAll();
        } else {
          alert(res.data.message || '删除失败');
        }
      } catch (e) {
        alert('删除失败');
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
  justify-content: space-between;
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
}

.def {
  max-width: 280px;
  white-space: pre-wrap;
  word-break: break-word;
}

.actions {
  white-space: nowrap;
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
  width: min(520px, 100%);
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
