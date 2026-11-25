import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  LucideChartBar,
  LucideMapPin,
  LucidePencil,
  LucidePlus,
  LucideRefreshCw,
  LucideSearch,
  LucideShieldCheck,
  LucideTrash2,
  LucideUserPlus,
  LucideUsers,
} from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import {
  AdminService,
  CidadeAdmin,
  LinhaTotalizacao,
  MembroAdmin,
  Relatorios,
  UFS_BRASIL,
} from '../../services/admin.service';

type Aba = 'relatorios' | 'cidades' | 'membros';

/**
 * Área de Administração (só o Pastor).
 * - Relatórios: participação dos membros (totalização), livros por autor (agrupamento) e reações por post (pivot).
 * - Cidades: cadastro completo (listar com filtro, adicionar, editar, excluir).
 * - Membros: cadastro completo de usuários (listar com filtro, adicionar, editar papel/cidade, excluir).
 */
@Component({
  selector: 'app-admin-page',
  standalone: true,
  imports: [
    FormsModule,
    LucideShieldCheck,
    LucideChartBar,
    LucideMapPin,
    LucideSearch,
    LucidePlus,
    LucideRefreshCw,
    LucidePencil,
    LucideTrash2,
    LucideUsers,
    LucideUserPlus,
  ],
  template: `
    <div class="px-4 py-6 sm:px-6">
      <header class="border-b border-slate-200 pb-5">
        <p class="flex items-center gap-1.5 text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
          <svg lucideShieldCheck [size]="13"></svg>
          <span>Área do Pastor</span>
        </p>
        <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">Administração</h1>

        <div class="mt-4 flex items-end justify-between gap-3">
        <div class="flex gap-4 sm:gap-8">
          @for (item of abas; track item.id) {
            <button
              type="button"
              (click)="trocarAba(item.id)"
              class="-mb-[21px] flex items-center gap-1.5 border-b-2 pb-3 text-sm font-semibold transition"
              [class.border-slate-800]="aba() === item.id"
              [class.text-slate-900]="aba() === item.id"
              [class.border-transparent]="aba() !== item.id"
              [class.text-slate-400]="aba() !== item.id"
            >
              @if (item.id === 'relatorios') {
                <svg lucideChartBar [size]="15"></svg>
              } @else if (item.id === 'membros') {
                <svg lucideUsers [size]="15"></svg>
              } @else {
                <svg lucideMapPin [size]="15"></svg>
              }
              <span>{{ item.rotulo }}</span>
            </button>
          }
        </div>
        <button
          type="button"
          (click)="atualizarAba()"
          [disabled]="atualizando()"
          title="Buscar de novo os dados desta aba"
          aria-label="Atualizar"
          class="mb-2 inline-flex shrink-0 items-center gap-1.5 rounded-lg border border-slate-200 px-3 py-1.5 text-xs font-semibold text-slate-600 transition hover:bg-slate-50 disabled:opacity-50"
        >
          <svg lucideRefreshCw [size]="13" [class.animate-spin]="atualizando()"></svg>
          <span class="hidden sm:inline">Atualizar</span>
        </button>
        </div>
      </header>

      @if (aba() !== 'relatorios' && mensagem(); as m) {
        <p
          class="mt-5 rounded-xl border px-4 py-2.5 text-xs font-medium"
          [class.border-rose-200]="m.tipo === 'erro'"
          [class.bg-rose-50]="m.tipo === 'erro'"
          [class.text-rose-700]="m.tipo === 'erro'"
          [class.border-emerald-200]="m.tipo === 'ok'"
          [class.bg-emerald-50]="m.tipo === 'ok'"
          [class.text-emerald-700]="m.tipo === 'ok'"
        >
          {{ m.texto }}
        </p>
      }

      <!-- ===================== RELATÓRIOS ===================== -->
      @if (aba() === 'relatorios') {
        <div class="mt-6 space-y-6">
          @if (carregandoRelatorios()) {
            <p class="py-10 text-center text-sm text-slate-500">Carregando relatórios...</p>
          } @else if (erroRelatorios()) {
            <div class="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
              {{ erroRelatorios() }}
              <button type="button" (click)="carregarRelatorios()" class="ml-2 font-semibold underline">
                Tentar de novo
              </button>
            </div>
          } @else if (relatorios(); as r) {
            <!-- 1. Totalização -->
            <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
              <div class="border-b border-slate-100 px-5 py-4">
                <h2 class="text-sm font-bold text-slate-900">Participação dos membros</h2>
                <p class="mt-0.5 text-xs text-slate-500">
                  Posts, comentários e reações de cada membro, somando dados de várias tabelas.
                </p>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left text-xs">
                  <thead class="bg-slate-50 text-[11px] tracking-wide text-slate-500 uppercase">
                    <tr>
                      <th class="px-5 py-2.5 font-semibold">Membro</th>
                      <th class="px-3 py-2.5 font-semibold">E-mail</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Posts</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Comentários</th>
                      <th class="px-5 py-2.5 text-right font-semibold">Reações</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    @for (l of r.totalizacao; track l.usuarioId) {
                      <tr>
                        <td class="px-5 py-2.5 font-medium text-slate-900">{{ l.autorNome }}</td>
                        <td class="px-3 py-2.5 text-slate-500">{{ l.email }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalPosts }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalComentarios }}</td>
                        <td class="px-5 py-2.5 text-right tabular-nums">{{ l.totalReacoes }}</td>
                      </tr>
                    } @empty {
                      <tr><td colspan="5" class="px-5 py-6 text-center text-slate-500">Nenhum membro cadastrado.</td></tr>
                    }
                  </tbody>
                  @if (r.totalizacao.length > 0) {
                    <tfoot class="border-t border-slate-200 bg-slate-50 font-semibold text-slate-900">
                      <tr>
                        <td class="px-5 py-2.5" colspan="2">Total ({{ r.totalizacao.length }} membros)</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ totais().posts }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ totais().comentarios }}</td>
                        <td class="px-5 py-2.5 text-right tabular-nums">{{ totais().reacoes }}</td>
                      </tr>
                    </tfoot>
                  }
                </table>
              </div>
            </section>

            <!-- 2. Agrupamento -->
            <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
              <div class="border-b border-slate-100 px-5 py-4">
                <h2 class="text-sm font-bold text-slate-900">Livros por autor</h2>
                <p class="mt-0.5 text-xs text-slate-500">
                  Livros e capítulos agrupados por autor (só quem tem pelo menos um livro).
                </p>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left text-xs">
                  <thead class="bg-slate-50 text-[11px] tracking-wide text-slate-500 uppercase">
                    <tr>
                      <th class="px-5 py-2.5 font-semibold">Autor</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Livros</th>
                      <th class="px-5 py-2.5 text-right font-semibold">Capítulos</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    @for (l of r.agrupamento; track l.autorNome) {
                      <tr>
                        <td class="px-5 py-2.5 font-medium text-slate-900">{{ l.autorNome }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalLivros }}</td>
                        <td class="px-5 py-2.5 text-right tabular-nums">{{ l.totalCapitulos }}</td>
                      </tr>
                    } @empty {
                      <tr><td colspan="3" class="px-5 py-6 text-center text-slate-500">Nenhum livro publicado ainda.</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </section>

            <!-- 3. Pivot -->
            <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
              <div class="border-b border-slate-100 px-5 py-4">
                <h2 class="text-sm font-bold text-slate-900">Reações por post</h2>
                <p class="mt-0.5 text-xs text-slate-500">
                  Cada tipo de reação vira uma coluna (tabela cruzada), do post mais reagido para o menos.
                </p>
              </div>
              <div class="overflow-x-auto">
                <table class="w-full text-left text-xs">
                  <thead class="bg-slate-50 text-[11px] tracking-wide text-slate-500 uppercase">
                    <tr>
                      <th class="px-5 py-2.5 font-semibold">Post</th>
                      <th class="px-3 py-2.5 font-semibold">Autor</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Amém</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Curtir</th>
                      <th class="px-3 py-2.5 text-right font-semibold">Orando</th>
                      <th class="px-5 py-2.5 text-right font-semibold">Total</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    @for (l of r.pivot; track l.postId) {
                      <tr>
                        <td class="max-w-xs px-5 py-2.5 text-slate-700" [title]="l.postTrecho">{{ trecho(l.postTrecho) }}</td>
                        <td class="px-3 py-2.5 whitespace-nowrap text-slate-500">{{ l.autorNome }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalAmem }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalCurtir }}</td>
                        <td class="px-3 py-2.5 text-right tabular-nums">{{ l.totalOrando }}</td>
                        <td class="px-5 py-2.5 text-right font-semibold tabular-nums text-slate-900">{{ l.totalReacoesGerais }}</td>
                      </tr>
                    } @empty {
                      <tr><td colspan="6" class="px-5 py-6 text-center text-slate-500">Nenhum post publicado ainda.</td></tr>
                    }
                  </tbody>
                </table>
              </div>
            </section>
          }
        </div>
      }

      <!-- ===================== CIDADES ===================== -->
      @if (aba() === 'cidades') {
        <div class="mt-6 space-y-5">
          <!-- Formulário único: adicionar ou editar -->
          <form
            id="form-cidade"
            (ngSubmit)="salvarCidade()"
            class="scroll-mt-6 rounded-2xl border bg-white p-4"
            [class.border-slate-200]="!cidadeEditando()"
            [class.border-slate-800]="cidadeEditando()"
          >
            <p class="mb-3 flex items-center gap-1.5 text-xs font-bold text-slate-900">
              @if (cidadeEditando(); as c) {
                <svg lucidePencil [size]="14"></svg>
                <span>Editando {{ c.nome }}/{{ c.uf }}</span>
              } @else {
                <svg lucidePlus [size]="14"></svg>
                <span>Nova cidade</span>
              }
            </p>
            <div class="flex flex-wrap items-end gap-3">
              <div class="min-w-40 flex-1">
                <label for="cidade-nome" class="mb-1 block text-xs font-semibold text-slate-600">Cidade *</label>
                <input id="cidade-nome" name="cidadeNome" type="text" maxlength="50" [(ngModel)]="formCidade.nome" placeholder="Ex.: Assis"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none" />
              </div>
              <div>
                <label for="cidade-uf" class="mb-1 block text-xs font-semibold text-slate-600">UF</label>
                <select id="cidade-uf" name="cidadeUf" [(ngModel)]="formCidade.uf"
                  class="rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs focus:border-slate-400 focus:outline-none">
                  @for (uf of ufs; track uf) {
                    <option [value]="uf">{{ uf }}</option>
                  }
                </select>
              </div>
              <div class="flex gap-2">
                <button type="submit" [disabled]="salvando() || !formCidade.nome.trim()"
                  class="inline-flex items-center gap-1.5 rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:opacity-40">
                  @if (cidadeEditando()) {
                    <span>Salvar alterações</span>
                  } @else {
                    <svg lucidePlus [size]="14"></svg>
                    <span>Adicionar</span>
                  }
                </button>
                @if (cidadeEditando()) {
                  <button type="button" (click)="cancelarCidade()"
                    class="rounded-xl border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-50">
                    Cancelar
                  </button>
                }
              </div>
            </div>
          </form>

          <!-- Filtro + lista -->
          <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
            <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-3.5">
              <h2 class="text-sm font-bold text-slate-900">
                Cidades cadastradas
                <span class="ml-1 text-xs font-medium text-slate-400">({{ cidades().length }})</span>
              </h2>
              <label class="relative block w-full sm:w-64">
                <span class="sr-only">Buscar cidade</span>
                <svg lucideSearch [size]="14" class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400"></svg>
                <input type="search" [ngModel]="busca()" (ngModelChange)="aoBuscar($event)" placeholder="Buscar pelo nome..."
                  class="w-full rounded-xl border border-slate-200 py-2 pr-3 pl-8 text-xs focus:border-slate-400 focus:outline-none" />
              </label>
            </div>
            <table class="w-full text-left text-xs">
              <thead class="bg-slate-50 text-[11px] tracking-wide text-slate-500 uppercase">
                <tr>
                  <th class="px-5 py-2.5 font-semibold">Cidade</th>
                  <th class="px-3 py-2.5 font-semibold">UF</th>
                  <th class="px-3 py-2.5 text-right font-semibold">Membros</th>
                  <th class="w-24 px-5 py-2.5 text-right font-semibold">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                @for (c of cidades(); track c.id) {
                  <tr [class.bg-slate-50]="cidadeEditando()?.id === c.id">
                    <td class="px-5 py-2.5 font-medium text-slate-900">{{ c.nome }}</td>
                    <td class="px-3 py-2.5 text-slate-600">{{ c.uf || '—' }}</td>
                    <td class="px-3 py-2.5 text-right tabular-nums">{{ c.membros }}</td>
                    <td class="px-5 py-2.5 text-right whitespace-nowrap">
                      <button type="button" (click)="editarCidade(c)" [attr.aria-label]="'Editar ' + c.nome" title="Editar"
                        class="rounded-lg p-1.5 text-slate-500 transition hover:bg-slate-100 hover:text-slate-900">
                        <svg lucidePencil [size]="14"></svg>
                      </button>
                      <button type="button" (click)="excluir(c)" [disabled]="c.membros > 0 || salvando()" [attr.aria-label]="'Excluir ' + c.nome"
                        [title]="c.membros > 0 ? 'Há membros morando nesta cidade' : 'Excluir'"
                        class="rounded-lg p-1.5 text-slate-400 transition hover:bg-rose-50 hover:text-rose-600 disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-slate-400">
                        <svg lucideTrash2 [size]="14"></svg>
                      </button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="4" class="px-5 py-6 text-center text-slate-500">
                      @if (carregandoCidades()) {
                        Carregando cidades...
                      } @else if (busca().trim()) {
                        Nenhuma cidade encontrada para "{{ busca().trim() }}".
                      } @else {
                        Nenhuma cidade cadastrada ainda.
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </section>
        </div>
      }

      <!-- ===================== MEMBROS ===================== -->
      @if (aba() === 'membros') {
        <div class="mt-6 space-y-5">
          <!-- Formulário único: adicionar ou editar (inclui a senha nova) -->
          <form
            id="form-membro"
            (ngSubmit)="salvarMembro()"
            class="grid scroll-mt-6 gap-3 rounded-2xl border bg-white p-4 sm:grid-cols-2 lg:grid-cols-3"
            [class.border-slate-200]="!membroEditando()"
            [class.border-slate-800]="membroEditando()"
          >
            <p class="flex items-center gap-1.5 text-xs font-bold text-slate-900 sm:col-span-2 lg:col-span-3">
              @if (membroEditando(); as m) {
                <svg lucidePencil [size]="14"></svg>
                <span>Editando {{ m.nome }}</span>
              } @else {
                <svg lucideUserPlus [size]="14"></svg>
                <span>Novo membro</span>
              }
            </p>
            <div>
              <label for="m-nome" class="mb-1 block text-xs font-semibold text-slate-600">Nome *</label>
              <input id="m-nome" name="mNome" type="text" maxlength="50" [(ngModel)]="form.nome"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none" />
            </div>
            <div>
              <label for="m-email" class="mb-1 block text-xs font-semibold text-slate-600">E-mail *</label>
              <input id="m-email" name="mEmail" type="email" maxlength="150" [(ngModel)]="form.email" [disabled]="!!membroEditando()"
                [title]="membroEditando() ? 'O e-mail é o login e não pode ser alterado' : ''"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none disabled:cursor-not-allowed disabled:bg-slate-50 disabled:text-slate-500" />
            </div>
            <div>
              <label for="m-senha" class="mb-1 block text-xs font-semibold text-slate-600">
                {{ membroEditando() ? 'Nova senha (opcional)' : 'Senha inicial * (mín. 6)' }}
              </label>
              <input id="m-senha" name="mSenha" type="text" autocomplete="off" spellcheck="false" [(ngModel)]="form.senha"
                [placeholder]="membroEditando() ? 'Em branco = mantém a atual' : 'Mínimo de 6 caracteres'"
                class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none" />
            </div>
            <div>
              <label for="m-papel" class="mb-1 block text-xs font-semibold text-slate-600">Papel</label>
              <select id="m-papel" name="mPapel" [(ngModel)]="form.papel" [disabled]="!!membroEditando() && membroEditando()?.id === meuId()"
                class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2 text-xs focus:border-slate-400 focus:outline-none disabled:opacity-50">
                <option value="MEMBRO">Membro</option>
                <option value="PASTOR">Pastor</option>
              </select>
            </div>
            <div class="grid grid-cols-3 gap-2">
              <div class="col-span-2">
                <label for="m-cidade" class="mb-1 block text-xs font-semibold text-slate-600">Cidade</label>
                <input id="m-cidade" name="mCidade" type="text" maxlength="50" list="lista-cidades" autocomplete="off"
                  [ngModel]="form.cidade" (ngModelChange)="aoDigitarCidade($event)" placeholder="Escolha ou digite"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none" />
                <datalist id="lista-cidades">
                  @for (c of cidadesSugestao(); track c.id) {
                    <option [value]="c.nome">{{ c.uf }}</option>
                  }
                </datalist>
              </div>
              <div>
                <label for="m-uf" class="mb-1 block text-xs font-semibold text-slate-600">UF</label>
                <select id="m-uf" name="mUf" [(ngModel)]="form.uf"
                  class="w-full rounded-xl border border-slate-200 bg-white px-2 py-2 text-xs focus:border-slate-400 focus:outline-none">
                  <option value="">—</option>
                  @for (uf of ufs; track uf) {
                    <option [value]="uf">{{ uf }}</option>
                  }
                </select>
              </div>
            </div>
            <div class="flex items-end gap-2">
              <button type="submit" [disabled]="salvando()"
                class="inline-flex flex-1 items-center justify-center gap-1.5 rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:opacity-40">
                @if (membroEditando()) {
                  <span>Salvar alterações</span>
                } @else {
                  <svg lucidePlus [size]="14"></svg>
                  <span>Adicionar membro</span>
                }
              </button>
              @if (membroEditando()) {
                <button type="button" (click)="cancelarMembro()"
                  class="rounded-xl border border-slate-200 px-4 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-50">
                  Cancelar
                </button>
              }
            </div>
          </form>

          <!-- Filtro + lista -->
          <section class="overflow-hidden rounded-2xl border border-slate-200 bg-white">
            <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-100 px-5 py-3.5">
              <h2 class="text-sm font-bold text-slate-900">
                Membros
                <span class="ml-1 text-xs font-medium text-slate-400">({{ membros().length }})</span>
              </h2>
              <label class="relative block w-full sm:w-64">
                <span class="sr-only">Buscar membro</span>
                <svg lucideSearch [size]="14" class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400"></svg>
                <input type="search" [ngModel]="buscaMembros()" (ngModelChange)="aoBuscarMembros($event)" placeholder="Buscar por nome ou e-mail..."
                  class="w-full rounded-xl border border-slate-200 py-2 pr-3 pl-8 text-xs focus:border-slate-400 focus:outline-none" />
              </label>
            </div>
            <table class="w-full text-left text-xs">
              <thead class="bg-slate-50 text-[11px] tracking-wide text-slate-500 uppercase">
                <tr>
                  <th class="px-5 py-2.5 font-semibold">Membro</th>
                  <th class="hidden px-3 py-2.5 font-semibold sm:table-cell">Cidade</th>
                  <th class="px-3 py-2.5 font-semibold">Papel</th>
                  <th class="hidden px-3 py-2.5 font-semibold md:table-cell">Desde</th>
                  <th class="w-24 px-5 py-2.5 text-right font-semibold">Ações</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-slate-100">
                @for (m of membros(); track m.id) {
                  <tr [class.bg-slate-50]="membroEditando()?.id === m.id">
                    <td class="px-5 py-2.5">
                      <p class="font-medium text-slate-900">
                        {{ m.nome }}
                        @if (m.id === meuId()) {
                          <span class="ml-1 text-[10px] font-medium text-slate-400">(você)</span>
                        }
                      </p>
                      <p class="mt-0.5 break-all text-[11px] text-slate-500">{{ m.email }}</p>
                    </td>
                    <td class="hidden px-3 py-2.5 text-slate-600 sm:table-cell">{{ m.cidade ? m.cidade + '/' + m.uf : '—' }}</td>
                    <td class="px-3 py-2.5">
                      <span class="rounded-full px-2 py-0.5 text-[11px] font-semibold"
                        [class.bg-slate-800]="m.papel === 'PASTOR'" [class.text-white]="m.papel === 'PASTOR'"
                        [class.bg-slate-100]="m.papel !== 'PASTOR'" [class.text-slate-600]="m.papel !== 'PASTOR'">
                        {{ m.papel === 'PASTOR' ? 'Pastor' : 'Membro' }}
                      </span>
                    </td>
                    <td class="hidden px-3 py-2.5 text-slate-500 md:table-cell"
                      [title]="m.membroDesde ? '' : 'Conta criada antes de o site registrar a data de cadastro'">{{ formatarData(m.membroDesde) }}</td>
                    <td class="px-5 py-2.5 text-right whitespace-nowrap">
                      <button type="button" (click)="editarMembro(m)" [attr.aria-label]="'Editar ' + m.nome" title="Editar (dados e senha)"
                        class="rounded-lg p-1.5 text-slate-500 transition hover:bg-slate-100 hover:text-slate-900">
                        <svg lucidePencil [size]="14"></svg>
                      </button>
                      <button type="button" (click)="excluirMembro(m)" [disabled]="m.id === meuId() || salvando()" [attr.aria-label]="'Excluir ' + m.nome"
                        [title]="m.id === meuId() ? 'Você não pode excluir a sua própria conta' : 'Excluir'"
                        class="rounded-lg p-1.5 text-slate-400 transition hover:bg-rose-50 hover:text-rose-600 disabled:cursor-not-allowed disabled:opacity-30 disabled:hover:bg-transparent disabled:hover:text-slate-400">
                        <svg lucideTrash2 [size]="14"></svg>
                      </button>
                    </td>
                  </tr>
                } @empty {
                  <tr>
                    <td colspan="5" class="px-5 py-6 text-center text-slate-500">
                      @if (carregandoMembros()) {
                        Carregando membros...
                      } @else if (buscaMembros().trim()) {
                        Nenhum membro encontrado para "{{ buscaMembros().trim() }}".
                      } @else {
                        Nenhum membro cadastrado.
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </section>
        </div>
      }
    </div>
  `,
})
export class AdminPageComponent implements OnInit {
  private readonly admin = inject(AdminService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);
  private readonly auth = inject(AuthService);

  readonly abas: { id: Aba; rotulo: string }[] = [
    { id: 'relatorios', rotulo: 'Relatórios' },
    { id: 'membros', rotulo: 'Membros' },
    { id: 'cidades', rotulo: 'Cidades' },
  ];
  readonly ufs = UFS_BRASIL;
  readonly aba = signal<Aba>('relatorios');

  // Relatórios
  readonly relatorios = signal<Relatorios | null>(null);
  readonly carregandoRelatorios = signal(false);
  readonly erroRelatorios = signal<string | null>(null);
  readonly totais = computed(() => somar(this.relatorios()?.totalizacao ?? []));

  // Cidades
  readonly cidades = signal<CidadeAdmin[]>([]);
  readonly carregandoCidades = signal(false);
  readonly busca = signal('');
  readonly cidadeEditando = signal<CidadeAdmin | null>(null);
  readonly salvando = signal(false);
  readonly mensagem = signal<{ tipo: 'ok' | 'erro'; texto: string } | null>(null);
  formCidade = { nome: '', uf: 'SP' };
  private buscaTimer: ReturnType<typeof setTimeout> | undefined;

  /** Algum carregamento em andamento (gira o ícone do botão Atualizar). */
  readonly atualizando = computed(() => this.carregandoRelatorios() || this.carregandoCidades() || this.carregandoMembros());

  ngOnInit(): void {
    // ?aba=cidades abre direto no cadastro de cidades (e o botão Voltar do navegador funciona entre abas).
    // Toda vez que uma aba é aberta, os dados dela são buscados de novo (sem precisar de F5).
    this.route.queryParamMap.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((params) => {
      const pedida = params.get('aba');
      const aba: Aba = pedida === 'cidades' || pedida === 'membros' ? pedida : 'relatorios';
      this.aba.set(aba);
      this.mensagem.set(null);
      this.cancelarCidade();
      this.cancelarMembro();
      this.atualizarAba();
    });
  }

  trocarAba(aba: Aba): void {
    this.router.navigate([], { queryParams: { aba: aba === 'relatorios' ? null : aba }, queryParamsHandling: 'merge' });
  }

  /** Botão "Atualizar": busca de novo os dados da aba aberta. */
  atualizarAba(): void {
    const aba = this.aba();
    if (aba === 'relatorios') {
      if (!this.carregandoRelatorios()) this.carregarRelatorios();
    } else if (aba === 'cidades') {
      this.carregarCidades();
    } else {
      this.carregarMembros();
      this.carregarSugestoesCidades();
    }
  }

  async carregarRelatorios(): Promise<void> {
    this.carregandoRelatorios.set(true);
    this.erroRelatorios.set(null);
    try {
      this.relatorios.set(await this.admin.relatorios());
    } catch (e) {
      this.erroRelatorios.set((e as Error).message);
    } finally {
      this.carregandoRelatorios.set(false);
    }
  }

  trecho(texto: string): string {
    const limpo = (texto ?? '').replace(/\s+/g, ' ').trim();
    return limpo.length > 80 ? limpo.slice(0, 80) + '…' : limpo;
  }

  // ---------- Cidades ----------

  aoBuscar(termo: string): void {
    this.busca.set(termo);
    clearTimeout(this.buscaTimer);
    // Espera a pessoa parar de digitar para não chamar o servidor a cada letra.
    this.buscaTimer = setTimeout(() => this.carregarCidades(), 350);
  }

  async carregarCidades(): Promise<void> {
    this.carregandoCidades.set(true);
    try {
      this.cidades.set(await this.admin.listarCidades(this.busca()));
    } catch (e) {
      this.mostrar('erro', (e as Error).message);
    } finally {
      this.carregandoCidades.set(false);
    }
  }

  /** O lápis da tabela leva a cidade para o formulário do topo. */
  editarCidade(c: CidadeAdmin): void {
    this.cidadeEditando.set(c);
    this.formCidade = { nome: c.nome, uf: c.uf || 'SP' };
    this.mensagem.set(null);
    rolarAte('form-cidade', 'cidade-nome');
  }

  cancelarCidade(): void {
    this.cidadeEditando.set(null);
    this.formCidade = { nome: '', uf: this.formCidade?.uf || 'SP' };
  }

  /** Adiciona (formulário vazio) ou salva a edição (cidade escolhida no lápis). */
  async salvarCidade(): Promise<void> {
    const nome = this.formCidade.nome.trim();
    if (!nome || this.salvando()) return;
    const editando = this.cidadeEditando();
    await this.executar(async () => {
      const c = editando
        ? await this.admin.atualizarCidade(editando.id, nome, this.formCidade.uf)
        : await this.admin.criarCidade(nome, this.formCidade.uf);
      this.cidadeEditando.set(null);
      this.formCidade = { nome: '', uf: c.uf || 'SP' };
      await this.carregarCidades();
      this.mostrar('ok', editando ? `Cidade atualizada para ${c.nome}/${c.uf}.` : `Cidade ${c.nome}/${c.uf} cadastrada.`);
    });
  }

  async excluir(c: CidadeAdmin): Promise<void> {
    if (!confirm(`Excluir a cidade ${c.nome}/${c.uf}?`)) return;
    await this.executar(async () => {
      await this.admin.excluirCidade(c.id);
      if (this.cidadeEditando()?.id === c.id) this.cancelarCidade();
      await this.carregarCidades();
      this.mostrar('ok', `Cidade ${c.nome} excluída.`);
    });
  }

  // ---------- Membros ----------

  readonly membros = signal<MembroAdmin[]>([]);
  readonly carregandoMembros = signal(false);
  readonly buscaMembros = signal('');
  readonly membroEditando = signal<MembroAdmin | null>(null);
  /** Cidades já cadastradas, sugeridas no campo Cidade do formulário. */
  readonly cidadesSugestao = signal<CidadeAdmin[]>([]);
  readonly meuId = computed(() => this.auth.currentUser()?.idUsuario ?? null);
  form = membroVazio();
  private buscaMembrosTimer: ReturnType<typeof setTimeout> | undefined;

  aoBuscarMembros(termo: string): void {
    this.buscaMembros.set(termo);
    clearTimeout(this.buscaMembrosTimer);
    this.buscaMembrosTimer = setTimeout(() => this.carregarMembros(), 350);
  }

  async carregarMembros(): Promise<void> {
    this.carregandoMembros.set(true);
    try {
      this.membros.set(await this.admin.listarMembros(this.buscaMembros()));
    } catch (e) {
      this.mostrar('erro', (e as Error).message);
    } finally {
      this.carregandoMembros.set(false);
    }
  }

  async carregarSugestoesCidades(): Promise<void> {
    try {
      this.cidadesSugestao.set(await this.admin.listarCidades(''));
    } catch {
      // sem sugestões: o campo continua aceitando qualquer cidade digitada
    }
  }

  /** Ao escolher uma cidade já cadastrada, a UF é preenchida sozinha (quando o nome é único). */
  aoDigitarCidade(nome: string): void {
    this.form.cidade = nome;
    const alvo = nome.trim().toLowerCase();
    const iguais = this.cidadesSugestao().filter((c) => c.nome.toLowerCase() === alvo);
    if (iguais.length === 1) this.form.uf = iguais[0].uf;
  }

  /** O lápis da tabela leva o membro para o formulário do topo (dados e senha nova). */
  editarMembro(m: MembroAdmin): void {
    this.membroEditando.set(m);
    this.form = { nome: m.nome, email: m.email, senha: '', papel: m.papel, cidade: m.cidade, uf: m.uf };
    this.mensagem.set(null);
    rolarAte('form-membro', 'm-nome');
  }

  cancelarMembro(): void {
    this.membroEditando.set(null);
    this.form = membroVazio();
  }

  /**
   * Adiciona (formulário vazio) ou salva a edição. Na edição, se o Pastor digitar uma senha nova,
   * ela é gravada e some do formulário logo em seguida.
   */
  async salvarMembro(): Promise<void> {
    if (this.salvando()) return;
    const editando = this.membroEditando();
    const senha = this.form.senha;
    const erro = editando
      ? validarMembro(this.form.nome, this.form.cidade, this.form.uf, undefined, senha ? senha : undefined)
      : validarMembro(this.form.nome, this.form.cidade, this.form.uf, this.form.email, senha);
    if (erro) {
      this.mostrar('erro', erro);
      return;
    }
    await this.executar(async () => {
      const dados = { nome: this.form.nome.trim(), papel: this.form.papel, cidade: this.form.cidade.trim(), uf: this.form.uf };
      let texto: string;
      if (editando) {
        const atualizado = await this.admin.atualizarMembro(editando.id, dados);
        if (senha) await this.admin.redefinirSenhaMembro(editando.id, senha);
        texto = senha
          ? `Dados e senha de ${atualizado.nome} atualizados. Passe a nova senha para a pessoa.`
          : `Dados de ${atualizado.nome} atualizados.`;
      } else {
        const criado = await this.admin.criarMembro({ ...dados, email: this.form.email.trim(), senha });
        texto = `${criado.nome} foi cadastrado(a) como ${criado.papel === 'PASTOR' ? 'Pastor' : 'Membro'}.`;
      }
      this.cancelarMembro();
      await Promise.all([this.carregarMembros(), this.carregarSugestoesCidades()]);
      this.mostrar('ok', texto);
    });
  }

  async excluirMembro(m: MembroAdmin): Promise<void> {
    const aviso =
      `Excluir a conta de ${m.nome}?\n\n` +
      'Também serão apagados os posts, comentários, reações, grifos e livros dessa pessoa. ' +
      'Essa ação não pode ser desfeita.';
    if (!confirm(aviso)) return;
    await this.executar(async () => {
      await this.admin.excluirMembro(m.id);
      if (this.membroEditando()?.id === m.id) this.cancelarMembro();
      await this.carregarMembros();
      this.mostrar('ok', `A conta de ${m.nome} foi excluída.`);
    });
  }

  formatarData(iso: string): string {
    const [ano, mes, dia] = (iso || '').split('-');
    return ano && mes && dia ? `${dia}/${mes}/${ano}` : '—';
  }

  private async executar(acao: () => Promise<void>): Promise<void> {
    this.salvando.set(true);
    this.mensagem.set(null);
    try {
      await acao();
    } catch (e) {
      this.mostrar('erro', (e as Error).message);
    } finally {
      this.salvando.set(false);
    }
  }

  private mostrar(tipo: 'ok' | 'erro', texto: string): void {
    this.mensagem.set({ tipo, texto });
  }
}

function somar(linhas: LinhaTotalizacao[]) {
  return linhas.reduce(
    (acc, l) => ({
      posts: acc.posts + l.totalPosts,
      comentarios: acc.comentarios + l.totalComentarios,
      reacoes: acc.reacoes + l.totalReacoes,
    }),
    { posts: 0, comentarios: 0, reacoes: 0 },
  );
}

function membroVazio() {
  return { nome: '', email: '', senha: '', papel: 'MEMBRO' as 'PASTOR' | 'MEMBRO', cidade: '', uf: '' };
}

/** Validação do formulário de membro (as mesmas regras são conferidas de novo no backend). */
function validarMembro(nome: string, cidade: string, uf: string, email?: string, senha?: string): string | null {
  const n = nome.trim();
  if (n.length < 3 || n.length > 50) return 'O nome deve ter entre 3 e 50 caracteres.';
  if (email !== undefined && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return 'Informe um e-mail válido.';
  if (senha !== undefined && senha.length < 6) return 'A senha inicial deve ter no mínimo 6 caracteres.';
  if (!!cidade.trim() !== !!uf) return 'Preencha cidade e UF juntos (ou deixe os dois em branco).';
  return null;
}

/** Leva a tela até o formulário e põe o cursor no primeiro campo. */
function rolarAte(formId: string, campoId: string): void {
  setTimeout(() => {
    document.getElementById(formId)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    (document.getElementById(campoId) as HTMLInputElement | null)?.focus({ preventScroll: true });
  });
}
