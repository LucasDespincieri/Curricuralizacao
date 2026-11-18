import { Component, computed, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucideBookmark, LucideChevronLeft, LucideChevronRight } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-book-reader',
  standalone: true,
  imports: [RouterLink, LucideArrowLeft, LucideBookmark, LucideChevronLeft, LucideChevronRight],
  template: `
    @if (book(); as livro) {
      <div class="min-h-screen bg-[#FAF9F6]">
        <!-- Barra Superior do Leitor -->
        <header
          class="sticky top-0 z-20 flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 bg-white/95 px-6 py-3.5 backdrop-blur"
        >
          <a
            routerLink="/biblioteca"
            class="inline-flex items-center gap-2 text-xs font-semibold text-slate-700 transition hover:text-slate-900"
          >
            <svg lucideArrowLeft [size]="16"></svg>
            <span>Voltar para Biblioteca</span>
          </a>

          <div class="flex items-center gap-3">
            <span class="hidden text-xs font-medium text-slate-500 sm:inline">
              Seção {{ currentIndex() + 1 }} de {{ sections().length }}
            </span>

            <!-- Controles de tipografia -->
            <div class="flex items-center rounded-lg border border-slate-200 bg-slate-50 p-0.5">
              <button
                type="button"
                (click)="decreaseFont()"
                [disabled]="fontSize() <= 15"
                class="rounded-md px-2 py-1 text-xs font-semibold text-slate-700 hover:bg-white disabled:opacity-40"
              >
                A-
              </button>
              <button
                type="button"
                (click)="increaseFont()"
                [disabled]="fontSize() >= 24"
                class="rounded-md px-2 py-1 text-xs font-semibold text-slate-700 hover:bg-white disabled:opacity-40"
              >
                A+
              </button>
            </div>

            <button
              type="button"
              (click)="toggleSave()"
              class="inline-flex items-center gap-1.5 rounded-xl px-3.5 py-1.5 text-xs font-semibold transition"
              [class.bg-slate-900]="isSaved()"
              [class.text-white]="isSaved()"
              [class.border]="!isSaved()"
              [class.border-slate-200]="!isSaved()"
              [class.bg-white]="!isSaved()"
              [class.text-slate-800]="!isSaved()"
            >
              <svg lucideBookmark [size]="14"></svg>
              <span>{{ isSaved() ? 'Salvo na Estante' : 'Salvar na Estante' }}</span>
            </button>
          </div>
        </header>

        <!-- Área de Leitura (Sem comentários ou reações) -->
        <div class="mx-auto grid max-w-5xl gap-8 px-6 py-10 md:grid-cols-[220px_1fr]">
          <!-- Índice de Seções -->
          <aside class="space-y-1.5">
            <p class="mb-2 px-2 text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
              Seções
            </p>
            @for (sec of sections(); track sec.ordem; let i = $index) {
              <button
                type="button"
                (click)="currentIndex.set(i)"
                class="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-left text-xs font-medium transition"
                [class.bg-slate-800]="currentIndex() === i"
                [class.text-white]="currentIndex() === i"
                [class.text-slate-600]="currentIndex() !== i"
                [class.hover:bg-slate-200/60]="currentIndex() !== i"
              >
                <span class="shrink-0 opacity-75">0{{ i + 1 }}</span>
                <span class="truncate">{{ sec.tituloSecao }}</span>
              </button>
            }
          </aside>

          <!-- Texto Principal -->
          <article class="max-w-2xl">
            <p class="text-[11px] font-semibold tracking-widest text-amber-800/80 uppercase">
              Capítulo 0{{ currentIndex() + 1 }} · {{ livro.tempoLeituraMin || 10 }} min de leitura
            </p>
            <h1 class="mt-2 text-3xl font-bold tracking-tight text-slate-900">
              {{ livro.titulo }}
            </h1>
            <p class="mt-1 text-xs text-slate-500">por {{ livro.autorNome }}</p>

            <div class="my-6 h-px w-12 bg-amber-800/30"></div>

            @if (activeSection(); as section) {
              <h2 class="text-xl font-bold text-slate-900">{{ section.tituloSecao }}</h2>
              <div
                class="mt-5 space-y-5 whitespace-pre-line leading-relaxed text-slate-700"
                [style.fontSize.px]="fontSize()"
              >
                {{ section.texto }}
              </div>
            }

            <!-- Navegação entre capítulos -->
            <div class="mt-12 flex items-center justify-between border-t border-slate-200 pt-6">
              <button
                type="button"
                (click)="prevSection()"
                [disabled]="currentIndex() === 0"
                class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:opacity-40"
              >
                <svg lucideChevronLeft [size]="15"></svg>
                <span>Anterior</span>
              </button>

              <button
                type="button"
                (click)="nextSection()"
                [disabled]="currentIndex() >= sections().length - 1"
                class="inline-flex items-center gap-1.5 rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:opacity-40"
              >
                <span>Próxima seção</span>
                <svg lucideChevronRight [size]="15"></svg>
              </button>
            </div>
          </article>
        </div>
      </div>
    } @else {
      <div class="p-12 text-center">
        <p class="text-sm text-slate-600">Livro não encontrado.</p>
        <a
          routerLink="/biblioteca"
          class="mt-4 inline-block text-xs font-semibold text-slate-800 underline"
        >
          Voltar para a Biblioteca
        </a>
      </div>
    }
  `,
})
export class BookReaderComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly bookService = inject(BookService);
  private readonly auth = inject(AuthService);

  readonly currentIndex = signal(0);
  readonly fontSize = signal(18);

  readonly book = computed(() => {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    return this.bookService.getLivroById(id);
  });

  readonly sections = computed(() => {
    const b = this.book();
    if (b?.conteudoSecoes?.length) return b.conteudoSecoes;
    return [
      {
        ordem: 1,
        tituloSecao: 'Introdução',
        texto: 'Conteúdo desta obra em preparação.',
      },
    ];
  });

  readonly activeSection = computed(() => this.sections()[this.currentIndex()]);
  readonly isSaved = computed(() => {
    const b = this.book();
    return b ? this.bookService.isSaved(b.idLivros) : false;
  });

  increaseFont(): void {
    this.fontSize.update((s) => Math.min(24, s + 2));
  }

  decreaseFont(): void {
    this.fontSize.update((s) => Math.max(15, s - 2));
  }

  prevSection(): void {
    this.currentIndex.update((i) => Math.max(0, i - 1));
  }

  nextSection(): void {
    this.currentIndex.update((i) => Math.min(this.sections().length - 1, i + 1));
  }

  toggleSave(): void {
    const b = this.book();
    if (!b) return;
    const userId = this.auth.currentUser()?.idUsuario ?? 1;
    this.bookService.toggleEstante(b, userId);
  }
}
