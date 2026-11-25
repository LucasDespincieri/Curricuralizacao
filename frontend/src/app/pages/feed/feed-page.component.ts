import { Component, computed, DestroyRef, inject, OnDestroy, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { LucideSearch, LucideX } from '@lucide/angular';
import { PostCardComponent } from '../../components/post-card/post-card.component';
import { PostComposerComponent } from '../../components/post-composer/post-composer.component';
import { AuthService } from '../../services/auth.service';
import { CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-feed-page',
  standalone: true,
  imports: [PostComposerComponent, PostCardComponent, LucideSearch, LucideX],
  template: `
    <div>
      <!-- Header da Coluna Central -->
      <header class="sticky top-0 z-20 border-b border-slate-200 bg-white/95 px-6 pt-6 backdrop-blur">
        <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
          Espaço de Comunhão
        </p>
        <div class="mt-1 flex flex-wrap items-center justify-between gap-3">
          <h1 class="text-2xl font-bold tracking-tight text-slate-900">Início</h1>

          <!-- Formulário de Filtro: busca nos posts pelo texto -->
          <label class="relative block w-full sm:w-64">
            <span class="sr-only">Buscar nos posts</span>
            <svg lucideSearch [size]="14" class="pointer-events-none absolute top-1/2 left-3 -translate-y-1/2 text-slate-400"></svg>
            <input
              type="search"
              [value]="termo()"
              (input)="aoDigitar($any($event.target).value)"
              placeholder="Buscar nos posts..."
              class="w-full rounded-full border border-slate-200 bg-slate-50/60 py-2 pr-8 pl-8 text-xs text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:bg-white focus:outline-none [&::-webkit-search-cancel-button]:hidden"
            />
            @if (termo()) {
              <button
                type="button"
                (click)="limparBusca()"
                aria-label="Limpar busca"
                class="absolute top-1/2 right-2 -translate-y-1/2 rounded-full p-1 text-slate-400 hover:text-slate-700"
              >
                <svg lucideX [size]="12"></svg>
              </button>
            }
          </label>
        </div>

        <!-- Abas de Filtro -->
        <div class="mt-4 flex gap-8">
          <button
            type="button"
            (click)="activeTab.set('ALL')"
            class="border-b-2 pb-3 text-sm font-semibold transition"
            [class.border-slate-800]="activeTab() === 'ALL'"
            [class.text-slate-900]="activeTab() === 'ALL'"
            [class.border-transparent]="activeTab() !== 'ALL'"
            [class.text-slate-400]="activeTab() !== 'ALL'"
          >
            Todos os Posts
          </button>

          <button
            type="button"
            (click)="activeTab.set('PASTOR')"
            class="border-b-2 pb-3 text-sm font-semibold transition"
            [class.border-slate-800]="activeTab() === 'PASTOR'"
            [class.text-slate-900]="activeTab() === 'PASTOR'"
            [class.border-transparent]="activeTab() !== 'PASTOR'"
            [class.text-slate-400]="activeTab() !== 'PASTOR'"
          >
            Palavra do Pastor
          </button>
        </div>
      </header>

      <!-- Composer: na aba "Palavra do Pastor" só aparece para o Pastor (ADM) -->
      @if (podePublicarNaAba()) {
        <app-post-composer #composer />
      }

      <!-- Feed de Posts -->
      <div>
        @for (post of filteredPosts(); track post.idPost) {
          <app-post-card [post]="post" />
        } @empty {
          <div class="px-6 py-12 text-center text-sm text-slate-500">
            @if (community.carregandoPosts()) {
              {{ community.busca() ? 'Buscando...' : 'Carregando publicações...' }}
            } @else if (community.busca()) {
              Nenhum post encontrado para "{{ community.busca() }}".
            } @else if (activeTab() === 'PASTOR') {
              O Pastor ainda não publicou nada por aqui.
            } @else {
              Nenhuma publicação ainda. Seja o primeiro a compartilhar!
            }
          </div>
        }
      </div>
    </div>
  `,
})
export class FeedPageComponent implements OnDestroy {
  protected readonly community = inject(CommunityService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly composer = viewChild<PostComposerComponent>('composer');

  readonly activeTab = signal<'ALL' | 'PASTOR'>('ALL');

  /** Em "Todos os Posts" qualquer um vê o composer; em "Palavra do Pastor", só o Pastor. */
  readonly podePublicarNaAba = computed(
    () => this.activeTab() === 'ALL' || this.auth.currentUser()?.role === 'PASTOR',
  );

  /** Texto digitado no campo de busca (a busca em si roda no backend, com uma pequena espera). */
  readonly termo = signal(this.community.busca());
  private buscaTimer: ReturnType<typeof setTimeout> | undefined;

  aoDigitar(valor: string): void {
    this.termo.set(valor);
    clearTimeout(this.buscaTimer);
    this.buscaTimer = setTimeout(() => this.community.buscar(valor), 350);
  }

  limparBusca(): void {
    clearTimeout(this.buscaTimer);
    this.termo.set('');
    this.community.buscar('');
  }

  ngOnDestroy(): void {
    // Saiu do Início: na volta o feed aparece completo de novo.
    clearTimeout(this.buscaTimer);
    if (this.community.busca()) this.community.buscar('');
  }

  readonly filteredPosts = computed(() => {
    const all = this.community.posts();
    if (this.activeTab() === 'PASTOR') {
      return all.filter((p) => p.autorRole === 'PASTOR');
    }
    return all;
  });

  constructor() {
    // "Novo Post" na barra lateral leva para /inicio?novo=1: volta para "Todos os Posts" e foca o composer.
    this.route.queryParamMap.pipe(takeUntilDestroyed(inject(DestroyRef))).subscribe((params) => {
      if (params.get('novo') === '1') {
        this.activeTab.set('ALL');
        setTimeout(() => this.composer()?.focusInput(), 50);
      }
    });
  }
}
