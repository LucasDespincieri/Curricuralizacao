import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideArrowRight, LucideBookOpen, LucidePin } from '@lucide/angular';
import { BookService } from '../../services/book.service';
import { CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-right-widgets',
  standalone: true,
  imports: [RouterLink, LucidePin, LucideBookOpen, LucideArrowRight],
  template: `
    <div class="space-y-6">
      <!-- Widget 1: Mural de Avisos -->
      <section class="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
        <div class="mb-4 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <span class="flex h-7 w-7 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
              <svg lucidePin [size]="15"></svg>
            </span>
            <h2 class="text-sm font-bold text-slate-900">Mural de Avisos</h2>
          </div>
          <span class="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] font-medium text-slate-600">
            Pastor
          </span>
        </div>

        <div class="space-y-3.5">
          @for (aviso of community.avisos(); track aviso.id) {
            <article class="border-b border-slate-100 pb-3 last:border-b-0 last:pb-0">
              <div class="mb-1 flex items-center justify-between gap-2">
                <h3 class="text-xs font-semibold text-slate-900">{{ aviso.titulo }}</h3>
                <span class="shrink-0 text-[11px] text-slate-400">{{ aviso.data }}</span>
              </div>
              <p class="text-xs leading-relaxed text-slate-600">{{ aviso.descricao }}</p>
            </article>
          } @empty {
            <p class="text-xs leading-relaxed text-slate-500">
              Nenhum aviso no momento. Os posts do Pastor com #Aviso aparecem aqui.
            </p>
          }
        </div>
      </section>

      <!-- Widget 2: Leitura Recomendada -->
      @if (recommendedBook(); as book) {
        <section class="rounded-2xl border border-slate-200 bg-white p-5 shadow-xs">
          <div class="mb-3 flex items-center gap-2">
            <span class="flex h-7 w-7 items-center justify-center rounded-lg bg-slate-100 text-slate-700">
              <svg lucideBookOpen [size]="15"></svg>
            </span>
            <h2 class="text-sm font-bold text-slate-900">Leitura Recomendada</h2>
          </div>

          <div
            class="mb-4 flex h-28 items-end rounded-xl p-3.5 text-white"
            [style.backgroundColor]="book.corCapa || '#2A4065'"
          >
            <div class="rounded-md bg-[#E3D3B7] px-2.5 py-2 text-[10px] font-bold tracking-tight text-slate-900 shadow-sm">
              {{ book.titulo }}
            </div>
          </div>

          <h3 class="text-sm font-bold text-slate-900">{{ book.titulo }}</h3>
          <p class="mt-0.5 text-xs text-slate-500">por {{ book.autorNome }}</p>

          @if (book.descricao) {
            <p class="mt-2 line-clamp-2 text-xs leading-relaxed text-slate-600">
              {{ book.descricao }}
            </p>
          }

          <a
            [routerLink]="['/biblioteca/ler', book.idLivros]"
            class="mt-4 flex w-full items-center justify-center gap-1.5 rounded-xl bg-slate-800 px-3.5 py-2 text-xs font-semibold text-white transition hover:bg-slate-900"
          >
            <span>Ler Agora</span>
            <svg lucideArrowRight [size]="14"></svg>
          </a>
        </section>
      }
    </div>
  `,
})
export class RightWidgetsComponent {
  protected readonly community = inject(CommunityService);
  private readonly bookService = inject(BookService);

  protected readonly recommendedBook = computed(() => this.bookService.livros()[0]);
}
