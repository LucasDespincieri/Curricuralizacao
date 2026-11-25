import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { LucideBookmarkMinus, LucideBookOpen, LucideChevronRight } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-shelf',
  standalone: true,
  imports: [RouterLink, LucideBookOpen, LucideBookmarkMinus, LucideChevronRight],
  template: `
    <div class="px-6 py-6">
      <header class="border-b border-slate-200 pb-5">
        <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
          Leituras salvas
        </p>
        <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">Minha Estante</h1>
        <p class="mt-1 text-sm text-slate-500">
          Livros e estudos devocionais que você salvou para ler com calma.
        </p>
      </header>

      <div class="mt-6 grid gap-4 sm:grid-cols-2">
        @for (item of bookService.estante(); track item.idBiblioteca) {
          <article
            class="flex flex-col overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs"
          >
            <div
              class="tema-claro flex h-28 items-end p-4"
              [style.backgroundColor]="item.livro.corCapa || '#2A4065'"
            >
              <span class="rounded-md bg-[#E3D3B7] px-2.5 py-1 text-[10px] font-bold text-slate-900 uppercase">
                {{ item.livro.titulo }}
              </span>
            </div>

            <div class="flex flex-1 flex-col justify-between p-4">
              <div>
                <h2 class="text-base font-bold text-slate-900">{{ item.livro.titulo }}</h2>
                <p class="mt-0.5 text-xs text-slate-500">por {{ item.livro.autorNome }}</p>
                <p class="mt-1 text-[11px] text-slate-400">
                  {{ item.livro.secoes }} {{ item.livro.secoes === 1 ? 'seção' : 'seções' }}
                </p>
              </div>

              <div class="mt-4 flex items-center gap-2">
                <a
                  [routerLink]="['/biblioteca/ler', item.livro.idLivros]"
                  class="inline-flex flex-1 items-center justify-center gap-1 rounded-xl bg-slate-800 px-3 py-2 text-xs font-semibold text-white transition hover:bg-slate-900"
                >
                  <span>Ler Agora</span>
                  <svg lucideChevronRight [size]="14"></svg>
                </a>
                <button
                  type="button"
                  (click)="remove(item.livro)"
                  title="Remover da estante"
                  class="rounded-xl border border-slate-200 p-2 text-slate-500 transition hover:bg-slate-50 hover:text-rose-600"
                >
                  <svg lucideBookmarkMinus [size]="16"></svg>
                </button>
              </div>
            </div>
          </article>
        } @empty {
          <div
            class="col-span-full rounded-2xl border border-dashed border-slate-300 bg-slate-50 p-10 text-center"
          >
            <svg lucideBookOpen [size]="28" class="mx-auto text-slate-400"></svg>
            <p class="mt-3 text-sm font-semibold text-slate-700">Sua estante está vazia</p>
            <p class="mt-1 text-xs text-slate-500">
              Explore a Biblioteca de Livros para salvar estudos e leituras aqui.
            </p>
            <a
              routerLink="/biblioteca"
              class="mt-4 inline-block rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white hover:bg-slate-900"
            >
              Ir para Biblioteca
            </a>
          </div>
        }
      </div>
    </div>
  `,
})
export class ShelfComponent {
  protected readonly bookService = inject(BookService);
  private readonly auth = inject(AuthService);

  remove(livro: ReturnType<BookService['livros']>[number]): void {
    const userId = this.auth.currentUser()?.idUsuario ?? 1;
    this.bookService.toggleEstante(livro, userId);
  }
}
