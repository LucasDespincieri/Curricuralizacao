import { Component, ElementRef, inject, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideSend } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { CommunityService } from '../../services/community.service';

const QUICK_TAGS = ['#Oração', '#Aviso', '#Estudo', '#PalavraDoPastor'];

@Component({
  selector: 'app-post-composer',
  standalone: true,
  imports: [FormsModule, LucideSend],
  template: `
    @if (auth.currentUser(); as user) {
      <section class="border-b border-slate-200 px-6 py-5">
        <div class="flex gap-3.5">
          <div
            class="flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-slate-300 bg-slate-50 text-xs font-semibold text-slate-700"
          >
            {{ getInitials(user.nome) }}
          </div>

          <div class="min-w-0 flex-1 space-y-3">
            <input
              type="text"
              [(ngModel)]="titulo"
              maxlength="20"
              placeholder="Título opcional (máx. 20 caracteres)"
              class="w-full rounded-lg border border-slate-200 bg-slate-50/60 px-3 py-1.5 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:bg-white focus:outline-none"
            />

            <textarea
              #textareaEl
              [(ngModel)]="conteudo"
              rows="3"
              maxlength="5000"
              placeholder="Compartilhe uma reflexão ou pedido de oração..."
              class="w-full resize-none border-0 bg-transparent p-0 text-sm leading-relaxed text-slate-900 placeholder:text-slate-400 focus:outline-none"
            ></textarea>

            <div class="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-3">
              <div class="flex flex-wrap gap-1.5">
                @for (tag of quickTags; track tag) {
                  <button
                    type="button"
                    (click)="toggleTag(tag)"
                    class="rounded-full border px-2.5 py-1 text-xs font-medium transition"
                    [class.border-slate-800]="selectedTags().includes(tag)"
                    [class.bg-slate-800]="selectedTags().includes(tag)"
                    [class.text-white]="selectedTags().includes(tag)"
                    [class.border-slate-200]="!selectedTags().includes(tag)"
                    [class.bg-slate-50]="!selectedTags().includes(tag)"
                    [class.text-slate-600]="!selectedTags().includes(tag)"
                  >
                    {{ tag }}
                  </button>
                }
              </div>

              <button
                type="button"
                (click)="submit()"
                [disabled]="!conteudo().trim()"
                class="inline-flex items-center gap-1.5 rounded-full bg-slate-800 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <svg lucideSend [size]="14"></svg>
                <span>Publicar</span>
              </button>
            </div>
          </div>
        </div>
      </section>
    }
  `,
})
export class PostComposerComponent {
  protected readonly auth = inject(AuthService);
  private readonly community = inject(CommunityService);

  readonly quickTags = QUICK_TAGS;
  readonly titulo = signal('');
  readonly conteudo = signal('');
  readonly selectedTags = signal<string[]>([]);
  readonly postCreated = output<void>();

  private readonly textareaEl = viewChild<ElementRef<HTMLTextAreaElement>>('textareaEl');

  focusInput(): void {
    this.textareaEl()?.nativeElement.focus();
  }

  getInitials(nome: string): string {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  toggleTag(tag: string): void {
    this.selectedTags.update((tags) =>
      tags.includes(tag) ? tags.filter((t) => t !== tag) : [...tags, tag],
    );
  }

  submit(): void {
    const user = this.auth.currentUser();
    const text = this.conteudo().trim();
    if (!user || !text) return;

    const tags = this.selectedTags();
    const finalContent =
      tags.length > 0 && !tags.some((t) => text.includes(t))
        ? `${text}\n${tags.join(' ')}`
        : text;

    this.community.createPost(user, finalContent, this.titulo(), tags);
    this.titulo.set('');
    this.conteudo.set('');
    this.selectedTags.set([]);
    this.postCreated.emit();
  }
}
