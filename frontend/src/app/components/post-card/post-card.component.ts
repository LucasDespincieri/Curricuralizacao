import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import {
  LucideBookmark,
  LucideHeart,
  LucideMessageCircle,
  LucideSend,
  LucideThumbsUp,
  LucideTrash2,
} from '@lucide/angular';
import { Post } from '../../models/community.models';
import { AuthService } from '../../services/auth.service';
import { CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-post-card',
  standalone: true,
  imports: [
    FormsModule,
    LucideHeart,
    LucideThumbsUp,
    LucideMessageCircle,
    LucideBookmark,
    LucideSend,
    LucideTrash2,
  ],
  template: `
    <article class="border-b border-slate-200 px-6 py-5 transition hover:bg-slate-50/40">
      <div class="flex gap-3.5">
        <!-- Avatar -->
        <div
          class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-slate-300 bg-white text-xs font-semibold text-slate-700"
        >
          {{ getInitials(post().autorNome) }}
        </div>

        <div class="min-w-0 flex-1">
          <!-- Header -->
          <div class="flex flex-wrap items-center gap-2">
            <span class="text-sm font-bold text-slate-900">{{ post().autorNome }}</span>
            <span
              class="rounded-full px-2 py-0.5 text-[11px] font-semibold"
              [class.bg-slate-800]="post().autorRole === 'PASTOR'"
              [class.text-white]="post().autorRole === 'PASTOR'"
              [class.bg-slate-100]="post().autorRole === 'MEMBRO'"
              [class.text-slate-600]="post().autorRole === 'MEMBRO'"
            >
              {{ post().autorRole === 'PASTOR' ? 'Pastor' : 'Membro' }}
            </span>
            <span class="text-xs text-slate-400">· {{ formatRelativeTime(post().data) }}</span>
          </div>

          <!-- Corpo (clicável para expandir comentários) -->
          <div class="mt-2 cursor-pointer" (click)="toggleComments()">
            @if (post().titulo) {
              <h3 class="mb-1 text-sm font-bold text-slate-900">{{ post().titulo }}</h3>
            }
            <p class="whitespace-pre-line text-sm leading-relaxed text-slate-700">
              @for (part of formattedParts(); track $index) {
                @if (part.isTag) {
                  <span class="font-medium text-slate-800">{{ part.text }}</span>
                } @else {
                  <span>{{ part.text }}</span>
                }
              }
            </p>
          </div>

          <!-- Footer de Ações -->
          <div class="mt-3.5 flex items-center justify-between text-slate-500">
            <div class="flex items-center gap-5">
              <button
                type="button"
                (click)="community.toggleReaction(post().idPost, 'CURTIR')"
                class="inline-flex items-center gap-1.5 text-xs font-medium transition hover:text-rose-600"
                [class.text-rose-600]="post().reacoes.userReacted === 'CURTIR'"
              >
                <svg lucideHeart [size]="16"></svg>
                <span>{{ post().reacoes.curtir }}</span>
              </button>

              <button
                type="button"
                (click)="community.toggleReaction(post().idPost, 'AMEM')"
                class="inline-flex items-center gap-1.5 text-xs font-medium transition hover:text-slate-900"
                [class.font-semibold]="post().reacoes.userReacted === 'AMEM'"
                [class.text-slate-900]="post().reacoes.userReacted === 'AMEM'"
              >
                <svg lucideThumbsUp [size]="16"></svg>
                <span>Amém ({{ post().reacoes.amem }})</span>
              </button>

              <button
                type="button"
                (click)="toggleComments()"
                class="inline-flex items-center gap-1.5 text-xs font-medium transition hover:text-slate-900"
                [class.text-slate-900]="showComments()"
              >
                <svg lucideMessageCircle [size]="16"></svg>
                <span>{{ post().comentariosCount }}</span>
              </button>
            </div>

            <div class="flex items-center gap-1">
              <button
                type="button"
                (click)="community.toggleSavePost(post().idPost)"
                aria-label="Salvar post"
                class="rounded-lg p-1.5 transition hover:bg-slate-100"
                [class.text-slate-900]="post().saved"
                [class.text-slate-400]="!post().saved"
              >
                <svg lucideBookmark [size]="16"></svg>
              </button>

              @if (canDelete()) {
                <button
                  type="button"
                  (click)="community.deletePost(post().idPost)"
                  aria-label="Excluir post"
                  title="Excluir publicação"
                  class="rounded-lg p-1.5 text-slate-400 transition hover:bg-rose-50 hover:text-rose-600"
                >
                  <svg lucideTrash2 [size]="15"></svg>
                </button>
              }
            </div>
          </div>

          <!-- Thread de Comentários -->
          @if (showComments()) {
            <div class="mt-4 space-y-3 border-t border-slate-100 pt-3.5">
              @for (comment of comments(); track comment.idComentarios) {
                <div class="flex gap-2.5 rounded-xl bg-slate-50 p-3">
                  <div
                    class="flex h-7 w-7 shrink-0 items-center justify-center rounded-full border border-slate-200 bg-white text-[10px] font-semibold text-slate-700"
                  >
                    {{ getInitials(comment.autorNome) }}
                  </div>
                  <div class="min-w-0 flex-1">
                    <div class="flex items-center gap-1.5">
                      <span class="text-xs font-semibold text-slate-900">
                        {{ comment.autorNome }}
                      </span>
                      <span
                        class="rounded-full px-1.5 py-0.5 text-[10px] font-medium"
                        [class.bg-slate-800]="comment.autorRole === 'PASTOR'"
                        [class.text-white]="comment.autorRole === 'PASTOR'"
                        [class.bg-slate-200]="comment.autorRole === 'MEMBRO'"
                        [class.text-slate-600]="comment.autorRole === 'MEMBRO'"
                      >
                        {{ comment.autorRole === 'PASTOR' ? 'Pastor' : 'Membro' }}
                      </span>
                      <span class="text-[11px] text-slate-400">
                        · {{ formatRelativeTime(comment.data) }}
                      </span>
                    </div>
                    <p class="mt-1 text-xs leading-relaxed text-slate-700">
                      {{ comment.conteudo }}
                    </p>
                  </div>
                </div>
              } @empty {
                <p class="text-xs text-slate-400">
                  Nenhum comentário ainda. Seja o primeiro a responder.
                </p>
              }

              <!-- Input de resposta -->
              @if (auth.currentUser(); as user) {
                <div class="flex gap-2 pt-1">
                  <input
                    type="text"
                    [(ngModel)]="newComment"
                    (keyup.enter)="submitComment()"
                    placeholder="Escreva uma resposta..."
                    class="flex-1 rounded-full border border-slate-200 bg-white px-3.5 py-1.5 text-xs text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
                  />
                  <button
                    type="button"
                    (click)="submitComment()"
                    [disabled]="!newComment().trim()"
                    class="inline-flex items-center gap-1 rounded-full bg-slate-800 px-3 py-1.5 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:opacity-40"
                  >
                    <svg lucideSend [size]="12"></svg>
                    <span>Enviar</span>
                  </button>
                </div>
              }
            </div>
          }
        </div>
      </div>
    </article>
  `,
})
export class PostCardComponent {
  readonly post = input.required<Post>();
  protected readonly community = inject(CommunityService);
  protected readonly auth = inject(AuthService);

  readonly showComments = signal(false);
  readonly newComment = signal('');

  protected readonly comments = computed(() =>
    this.community.getCommentsForPost(this.post().idPost),
  );

  protected readonly canDelete = computed(() => {
    const user = this.auth.currentUser();
    if (!user) return false;
    return user.role === 'PASTOR' || user.idUsuario === this.post().idUsuario;
  });

  protected readonly formattedParts = computed(() => {
    const content = this.post().conteudo;
    return content.split(/(#[A-Za-zÀ-ÿ0-9_]+)/g).map((part) => ({
      text: part,
      isTag: part.startsWith('#') && part.length > 1,
    }));
  });

  toggleComments(): void {
    this.showComments.update((v) => !v);
  }

  submitComment(): void {
    const user = this.auth.currentUser();
    const text = this.newComment().trim();
    if (!user || !text) return;
    this.community.addComment(this.post().idPost, user, text);
    this.newComment.set('');
  }

  getInitials(nome: string): string {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  formatRelativeTime(isoDate: string): string {
    const diffMs = Date.now() - new Date(isoDate).getTime();
    const mins = Math.max(1, Math.floor(diffMs / 60000));
    if (mins < 60) return `há ${mins} min`;
    const hours = Math.floor(mins / 60);
    if (hours < 24) return `há ${hours} h`;
    const days = Math.floor(hours / 24);
    return `há ${days} d`;
  }
}
