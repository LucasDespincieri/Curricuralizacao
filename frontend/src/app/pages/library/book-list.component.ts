import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  LucideBookmark,
  LucideChevronRight,
  LucidePenLine,
  LucideSearch,
} from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { LoginPromptService } from '../../services/login-prompt.service';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    LucideSearch,
    LucidePenLine,
    LucideBookmark,
    LucideChevronRight,
  ],
  template: `
    <div class="px-6 py-6">
      <!-- Header -->
      <header class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
            Conheça, aprenda, compartilhe
          </p>
          <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">
            Biblioteca Comunitária &amp; Estudos
          </h1>
          <p class="mt-1 text-sm text-slate-500">
            Leituras para nutrir a fé e fortalecer os laços da nossa comunidade.
          </p>
        </div>

        <a
          routerLink="/biblioteca/novo"
          class="inline-flex items-center gap-2 rounded-xl bg-slate-800 px-4 py-2.5 text-xs font-semibold text-white shadow-xs transition hover:bg-slate-900"
        >
          <svg lucidePenLine [size]="15"></svg>
          <span>+ Escrever Livro</span>
        </a>
      </header>

      <!-- Busca e Filtros -->
      <div class="mt-6 flex flex-col gap-3 sm:flex-row sm:items-center">
        <div class="relative flex-1">
          <svg
            lucideSearch
            [size]="16"
            class="pointer-events-none absolute top-1/2 left-3.5 -translate-y-1/2 text-slate-400"
          ></svg>
          <input
            type="text"
            [(ngModel)]="searchTerm"
            placeholder="Buscar livros, estudos ou autores..."
            class="w-full rounded-xl border border-slate-200 bg-white py-2.5 pr-4 pl-10 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
          />
        </div>

        <div class="flex rounded-xl border border-slate-200 bg-slate-50 p-1">
          <button
            type="button"
            (click)="filterMode.set('ALL')"
            class="rounded-lg px-3 py-1.5 text-xs font-semibold transition"
            [class.bg-white]="filterMode() === 'ALL'"
            [class.text-slate-900]="filterMode() === 'ALL'"
            [class.shadow-xs]="filterMode() === 'ALL'"
            [class.text-slate-500]="filterMode() !== 'ALL'"
          >
            Todos
          </button>
          <button
            type="button"
            (click)="filterMode.set('PASTOR')"
            class="rounded-lg px-3 py-1.5 text-xs font-semibold transition"
            [class.bg-white]="filterMode() === 'PASTOR'"
            [class.text-slate-900]="filterMode() === 'PASTOR'"
            [class.shadow-xs]="filterMode() === 'PASTOR'"
            [class.text-slate-500]="filterMode() !== 'PASTOR'"
          >
            Escritos pelo Pastor
          </button>
          <button
            type="button"
            (click)="filterMode.set('COMUNIDADE')"
            class="rounded-lg px-3 py-1.5 text-xs font-semibold transition"
            [class.bg-white]="filterMode() === 'COMUNIDADE'"
            [class.text-slate-900]="filterMode() === 'COMUNIDADE'"
            [class.shadow-xs]="filterMode() === 'COMUNIDADE'"
            [class.text-slate-500]="filterMode() !== 'COMUNIDADE'"
          >
            Comunidade
          </button>
        </div>
      </div>

      <!-- Subtítulo da seção -->
      <div class="mt-7 mb-4 flex items-center justify-between">
        <h2 class="text-base font-bold text-slate-900">Explorar leituras</h2>
        <span class="text-xs text-slate-400">{{ filteredBooks().length }} títulos</span>
      </div>

      <!-- Grid de Livros -->
      <div class="grid gap-5 sm:grid-cols-2">
        @for (book of filteredBooks(); track book.idLivros) {
          <article
            class="flex flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs transition hover:border-slate-300"
          >
            <!-- Capa -->
            <div
              class="relative flex h-36 items-end p-4"
              [style.backgroundColor]="book.corCapa || '#2A4065'"
            >
              @if (book.tempoLeituraMin) {
                <span
                  class="absolute top-3 right-3 rounded-full bg-slate-900/35 px-2.5 py-1 text-[11px] font-medium text-white backdrop-blur-xs"
                >
                  {{ book.tempoLeituraMin }} min de leitura
                </span>
              }
              <div
                class="flex h-24 w-16 items-center justify-center rounded-md bg-[#E3D3B7] p-2 text-center text-[9px] leading-tight font-bold tracking-tight text-slate-900 uppercase shadow-md"
              >
                {{ book.titulo }}
              </div>
            </div>

            <!-- Conteúdo -->
            <div class="flex flex-1 flex-col justify-between p-4">
              <div>
                <div class="flex items-center gap-2.5">
                  <div
                    class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full border border-slate-200 bg-slate-50 text-[10px] font-semibold text-slate-700"
                  >
                    {{ getInitials(book.autorNome) }}
                  </div>
                  <div class="min-w-0">
                    <p class="truncate text-xs font-semibold text-slate-900">{{ book.autorNome }}</p>
                    <p class="text-[11px] text-slate-400">
                      {{ book.secoes }} {{ book.secoes === 1 ? 'seção' : 'seções' }} ·
                      {{ book.data_inicio }}
                    </p>
                  </div>
                </div>

                <h3 class="mt-3 text-base font-bold text-slate-900">{{ book.titulo }}</h3>
                @if (book.descricao) {
                  <p class="mt-1 line-clamp-2 text-xs leading-relaxed text-slate-600">
                    {{ book.descricao }}
                  </p>
                }
              </div>

              <!-- Ações -->
              <div class="mt-4 flex items-center gap-2">
                <a
                  [routerLink]="['/biblioteca/ler', book.idLivros]"
                  class="inline-flex flex-1 items-center justify-center gap-1 rounded-xl bg-slate-800 px-3 py-2 text-xs font-semibold text-white transition hover:bg-slate-900"
                >
                  <span>Ler Livro</span>
                  <svg lucideChevronRight [size]="14"></svg>
                </a>

                <button
                  type="button"
                  (click)="toggleSave(book)"
                  [title]="
                    bookService.isSaved(book.idLivros)
                      ? 'Remover da Estante'
                      : 'Salvar na Estante'
                  "
                  class="inline-flex items-center justify-center rounded-xl border p-2 transition"
                  [class.border-slate-800]="bookService.isSaved(book.idLivros)"
                  [class.bg-slate-800]="bookService.isSaved(book.idLivros)"
                  [class.text-white]="bookService.isSaved(book.idLivros)"
                  [class.border-slate-200]="!bookService.isSaved(book.idLivros)"
                  [class.text-slate-600]="!bookService.isSaved(book.idLivros)"
                  [class.hover:bg-slate-50]="!bookService.isSaved(book.idLivros)"
                >
                  <svg lucideBookmark [size]="16"></svg>
                </button>
              </div>
            </div>
          </article>
        } @empty {
          <div class="col-span-full py-12 text-center text-sm text-slate-500">
            Nenhum livro encontrado para a busca atual.
          </div>
        }
      </div>
    </div>
  `,
})
export class BookListComponent {
  protected readonly bookService = inject(BookService);
  private readonly auth = inject(AuthService);
  private readonly loginPrompt = inject(LoginPromptService);

  readonly searchTerm = signal('');
  readonly filterMode = signal<'ALL' | 'PASTOR' | 'COMUNIDADE'>('ALL');

  readonly filteredBooks = computed(() => {
    const query = this.searchTerm().trim().toLowerCase();
    const mode = this.filterMode();

    return this.bookService.livros().filter((book) => {
      if (mode === 'PASTOR' && book.autorRole !== 'PASTOR') return false;
      if (mode === 'COMUNIDADE' && book.autorRole !== 'MEMBRO') return false;
      if (!query) return true;
      return (
        book.titulo.toLowerCase().includes(query) ||
        book.autorNome.toLowerCase().includes(query)
      );
    });
  });

  toggleSave(book: ReturnType<BookService['livros']>[number]): void {
    if (!this.loginPrompt.exigirLogin('Entre na sua conta para guardar livros na sua estante.')) return;
    const userId = this.auth.currentUser()?.idUsuario ?? 1;
    this.bookService.toggleEstante(book, userId);
  }

  getInitials(nome: string): string {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }
}
