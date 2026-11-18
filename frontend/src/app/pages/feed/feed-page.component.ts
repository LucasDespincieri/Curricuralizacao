import { Component, computed, DestroyRef, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { PostCardComponent } from '../../components/post-card/post-card.component';
import { PostComposerComponent } from '../../components/post-composer/post-composer.component';
import { AuthService } from '../../services/auth.service';
import { CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-feed-page',
  standalone: true,
  imports: [PostComposerComponent, PostCardComponent],
  template: `
    <div>
      <!-- Header da Coluna Central -->
      <header class="sticky top-0 z-20 border-b border-slate-200 bg-white/95 px-6 pt-6 backdrop-blur">
        <p class="text-[11px] font-semibold tracking-widest text-slate-400 uppercase">
          Espaço de Comunhão
        </p>
        <h1 class="mt-1 text-2xl font-bold tracking-tight text-slate-900">Início</h1>

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
              Carregando publicações...
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
export class FeedPageComponent {
  protected readonly community = inject(CommunityService);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly composer = viewChild<PostComposerComponent>('composer');

  readonly activeTab = signal<'ALL' | 'PASTOR'>('ALL');

  /** Em "Todos os Posts" qualquer um vê o composer; em "Palavra do Pastor", só o Pastor. */
  readonly podePublicarNaAba = computed(
    () => this.activeTab() === 'ALL' || this.auth.currentUser()?.role === 'PASTOR',
  );

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
