import { Component, ElementRef, inject, OnDestroy, output, signal, viewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { LucideImagePlus, LucideLoaderCircle, LucideSend, LucideX } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { CommunityService } from '../../services/community.service';
import { LoginPromptService } from '../../services/login-prompt.service';
import { TIPOS_IMAGEM_ACEITOS, UploadService } from '../../services/upload.service';

const QUICK_TAGS = ['#Oração', '#Aviso', '#Estudo', '#PalavraDoPastor'];

@Component({
  selector: 'app-post-composer',
  standalone: true,
  imports: [FormsModule, LucideSend, LucideImagePlus, LucideLoaderCircle, LucideX],
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

            @if (previewUrl(); as preview) {
              <div class="relative overflow-hidden rounded-xl border border-slate-200 bg-slate-50">
                <img [src]="preview" alt="Pré-visualização da imagem" class="max-h-72 w-full object-cover" />
                @if (enviandoImagem()) {
                  <div class="absolute inset-0 flex items-center justify-center gap-2 bg-white/70 text-xs font-medium text-slate-700">
                    <svg lucideLoaderCircle [size]="16" class="animate-spin"></svg>
                    <span>Enviando imagem...</span>
                  </div>
                }
                <button
                  type="button"
                  (click)="removerImagem()"
                  aria-label="Remover imagem"
                  class="absolute right-2 top-2 rounded-full bg-slate-900/70 p-1 text-white transition hover:bg-slate-900"
                >
                  <svg lucideX [size]="14"></svg>
                </button>
              </div>
            }
            @if (erroImagem()) {
              <p class="text-xs font-medium text-rose-600">{{ erroImagem() }}</p>
            }

            <div class="flex flex-wrap items-center justify-between gap-3 border-t border-slate-100 pt-3">
              <div class="flex flex-wrap items-center gap-1.5">
                <input
                  #arquivoEl
                  type="file"
                  class="hidden"
                  [accept]="tiposAceitos"
                  (change)="aoSelecionarArquivo($event)"
                />
                <button
                  type="button"
                  (click)="arquivoEl.click()"
                  [disabled]="enviandoImagem()"
                  aria-label="Adicionar imagem"
                  title="Adicionar imagem"
                  class="rounded-full border border-slate-200 bg-slate-50 p-1.5 text-slate-600 transition hover:border-slate-400 hover:text-slate-900 disabled:opacity-40"
                >
                  <svg lucideImagePlus [size]="14"></svg>
                </button>
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
                [disabled]="!conteudo().trim() || enviandoImagem()"
                class="inline-flex items-center gap-1.5 rounded-full bg-slate-800 px-4 py-1.5 text-xs font-semibold text-white transition hover:bg-slate-900 disabled:cursor-not-allowed disabled:opacity-40"
              >
                <svg lucideSend [size]="14"></svg>
                <span>Publicar</span>
              </button>
            </div>
          </div>
        </div>
      </section>
    } @else {
      <section class="border-b border-slate-200 px-6 py-5">
        <button
          type="button"
          (click)="loginPrompt.exigirLogin('Entre na sua conta para compartilhar reflexões e pedidos de oração.')"
          class="w-full rounded-xl border border-dashed border-slate-300 bg-slate-50/60 px-4 py-3 text-left text-sm text-slate-500 transition hover:border-slate-400 hover:text-slate-700"
        >
          Entre na sua conta para compartilhar uma reflexão ou pedido de oração...
        </button>
      </section>
    }
  `,
})
export class PostComposerComponent implements OnDestroy {
  protected readonly auth = inject(AuthService);
  private readonly community = inject(CommunityService);
  private readonly upload = inject(UploadService);
  protected readonly loginPrompt = inject(LoginPromptService);

  readonly quickTags = QUICK_TAGS;
  readonly titulo = signal('');
  readonly conteudo = signal('');
  readonly selectedTags = signal<string[]>([]);

  // Estado da imagem anexada
  readonly tiposAceitos = TIPOS_IMAGEM_ACEITOS.join(',');
  readonly previewUrl = signal<string | null>(null);
  readonly imagemUrl = signal<string | null>(null);
  readonly enviandoImagem = signal(false);
  readonly erroImagem = signal<string | null>(null);
  private envioAtual = 0;
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

  async aoSelecionarArquivo(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0];
    input.value = ''; // permite escolher o mesmo arquivo de novo depois de remover
    if (!arquivo) return;

    const erro = this.upload.validarImagem(arquivo);
    if (erro) {
      this.erroImagem.set(erro);
      return;
    }

    this.limparPreview();
    this.erroImagem.set(null);
    this.descartarEnviada(); // trocou de imagem: a anterior não vai ser usada
    this.previewUrl.set(URL.createObjectURL(arquivo));
    this.enviandoImagem.set(true);

    // Se o usuário trocar de imagem no meio do envio, ignora a resposta antiga.
    const envio = ++this.envioAtual;
    try {
      const url = await this.upload.enviarImagemPost(arquivo);
      if (envio === this.envioAtual) {
        this.imagemUrl.set(url);
      } else {
        this.upload.descartarImagem(url); // o usuário removeu/trocou a imagem durante o envio
      }
    } catch (e) {
      if (envio === this.envioAtual) {
        this.limparPreview();
        this.erroImagem.set(
          e instanceof Error && e.message ? e.message : 'Não foi possível enviar a imagem.',
        );
      }
    } finally {
      if (envio === this.envioAtual) this.enviandoImagem.set(false);
    }
  }

  /** X da pré-visualização: tira a imagem do post e apaga ela do Cloudinary. */
  removerImagem(): void {
    this.descartarEnviada();
    this.resetarImagem();
  }

  /** Limpa o estado da imagem SEM apagar do Cloudinary (usado depois de publicar). */
  private resetarImagem(): void {
    this.envioAtual++;
    this.limparPreview();
    this.imagemUrl.set(null);
    this.enviandoImagem.set(false);
    this.erroImagem.set(null);
  }

  private descartarEnviada(): void {
    const url = this.imagemUrl();
    if (url) this.upload.descartarImagem(url);
    this.imagemUrl.set(null);
  }

  ngOnDestroy(): void {
    // Saiu da página com uma imagem enviada e não publicada: apaga para não sobrar no Cloudinary.
    this.descartarEnviada();
    this.limparPreview();
  }

  private limparPreview(): void {
    const atual = this.previewUrl();
    if (atual) URL.revokeObjectURL(atual);
    this.previewUrl.set(null);
  }

  toggleTag(tag: string): void {
    this.selectedTags.update((tags) =>
      tags.includes(tag) ? tags.filter((t) => t !== tag) : [...tags, tag],
    );
  }

  submit(): void {
    const user = this.auth.currentUser();
    const text = this.conteudo().trim();
    if (!user || !text || this.enviandoImagem()) return;

    const tags = this.selectedTags();
    const finalContent =
      tags.length > 0 && !tags.some((t) => text.includes(t))
        ? `${text}\n${tags.join(' ')}`
        : text;

    this.community.createPost(user, finalContent, this.titulo(), tags, this.imagemUrl());
    this.titulo.set('');
    this.conteudo.set('');
    this.selectedTags.set([]);
    this.resetarImagem(); // a imagem agora pertence ao post: não pode ser apagada
    this.postCreated.emit();
  }
}
