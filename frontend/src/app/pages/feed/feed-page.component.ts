import { Component, computed, effect, inject, signal, viewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PostCardComponent } from '../../components/post-card/post-card.component';
import { PostComposerComponent } from '../../components/post-composer/post-composer.component';
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

      <!-- Composer -->
      <app-post-composer #composer />

      <!-- Feed de Posts -->
      <div>
        @for (post of filteredPosts(); track post.idPost) {
          <app-post-card [post]="post" />
        } @empty {
          <div class="px-6 py-12 text-center text-sm text-slate-500">
            Nenhuma publicação encontrada nesta aba.
          </div>
        }
      </div>
    </div>
  `,
})
export class FeedPageComponent {
  private readonly community = inject(CommunityService);
  private readonly route = inject(ActivatedRoute);
  private readonly composer = viewChild<PostComposerComponent>('composer');

  readonly activeTab = signal<'ALL' | 'PASTOR'>('ALL');

  readonly filteredPosts = computed(() => {
    const all = this.community.posts();
    if (this.activeTab() === 'PASTOR') {
      return all.filter((p) => p.autorRole === 'PASTOR');
    }
    return all;
  });

  constructor() {
    effect(() => {
      const novo = this.route.snapshot.queryParamMap.get('novo');
      if (novo === '1') {
        setTimeout(() => this.composer()?.focusInput(), 50);
      }
    });
  }
}
