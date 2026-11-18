import { Component, inject, OnInit, signal } from '@angular/core';
import { FormArray, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { LucideArrowLeft, LucidePlus, LucideSave, LucideTrash2 } from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';

@Component({
  selector: 'app-book-editor',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, LucideArrowLeft, LucidePlus, LucideTrash2, LucideSave],
  template: `
    <div class="px-6 py-6">
      <header class="mb-6 flex flex-wrap items-center justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <a
            routerLink="/biblioteca"
            class="mb-2 inline-flex items-center gap-1.5 text-xs font-semibold text-slate-500 hover:text-slate-900"
          >
            <svg lucideArrowLeft [size]="15"></svg>
            <span>Voltar para Biblioteca</span>
          </a>
          <h1 class="text-2xl font-bold tracking-tight text-slate-900">
            {{ editingId() ? 'Editar Livro / Estudo' : 'Escrever Novo Livro / Estudo' }}
          </h1>
        </div>

        <div class="flex items-center gap-2.5">
          <button
            type="button"
            (click)="onSubmit(false)"
            [disabled]="form.invalid"
            class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-white px-4 py-2 text-xs font-semibold text-slate-700 transition hover:bg-slate-50 disabled:opacity-40"
          >
            <svg lucideSave [size]="14"></svg>
            <span>Salvar Alterações</span>
          </button>

          <button
            type="button"
            (click)="onSubmit(true)"
            [disabled]="form.invalid"
            class="inline-flex items-center gap-1.5 rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white shadow-xs transition hover:bg-slate-900 disabled:opacity-40"
          >
            <span>Publicar</span>
          </button>
        </div>
      </header>

      <form [formGroup]="form" class="space-y-6">
        <!-- Metadados do Livro -->
        <div class="space-y-4 rounded-2xl border border-slate-200 bg-white p-5">
          <div>
            <div class="mb-1.5 flex items-center justify-between">
              <label for="titulo" class="text-xs font-semibold text-slate-700">
                Título do Livro *
              </label>
              <span class="text-[11px] text-slate-400">
                {{ form.controls.titulo.value.length || 0 }}/200
              </span>
            </div>
            <input
              id="titulo"
              type="text"
              formControlName="titulo"
              maxlength="200"
              placeholder="Ex.: Caminhos de Graça"
              class="w-full rounded-xl border border-slate-200 px-3.5 py-2.5 text-sm font-medium text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
            />
          </div>

          <div>
            <label for="descricao" class="mb-1.5 block text-xs font-semibold text-slate-700">
              Breve Descrição
            </label>
            <input
              id="descricao"
              type="text"
              formControlName="descricao"
              maxlength="280"
              placeholder="Resumo do estudo ou devocional..."
              class="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
            />
          </div>
        </div>

        <!-- Gerenciador de Seções / Capítulos -->
        <div class="rounded-2xl border border-slate-200 bg-white p-5">
          <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
            <h2 class="text-sm font-bold text-slate-900">Seções / Capítulos</h2>
            <button
              type="button"
              (click)="addSection()"
              class="inline-flex items-center gap-1.5 rounded-xl border border-slate-200 bg-slate-50 px-3 py-1.5 text-xs font-semibold text-slate-700 transition hover:bg-slate-100"
            >
              <svg lucidePlus [size]="14"></svg>
              <span>Adicionar Seção</span>
            </button>
          </div>

          <!-- Abas de Navegação entre Seções -->
          <div class="mb-5 flex flex-wrap gap-2">
            @for (sec of secoes.controls; track $index; let i = $index) {
              <button
                type="button"
                (click)="activeSectionIndex.set(i)"
                class="rounded-xl border px-3 py-1.5 text-xs font-semibold transition"
                [class.border-slate-800]="activeSectionIndex() === i"
                [class.bg-slate-800]="activeSectionIndex() === i"
                [class.text-white]="activeSectionIndex() === i"
                [class.border-slate-200]="activeSectionIndex() !== i"
                [class.bg-white]="activeSectionIndex() !== i"
                [class.text-slate-600]="activeSectionIndex() !== i"
              >
                Seção {{ i + 1 }}
              </button>
            }
          </div>

          <!-- Editor da Seção Ativa -->
          <div formArrayName="secoes">
            @for (sec of secoes.controls; track $index; let i = $index) {
              @if (activeSectionIndex() === i) {
                <div [formGroupName]="i" class="space-y-4">
                  <div class="flex items-center justify-between gap-3">
                    <div class="flex-1">
                      <label class="mb-1.5 block text-xs font-semibold text-slate-700">
                        Título da Seção {{ i + 1 }} *
                      </label>
                      <input
                        type="text"
                        formControlName="tituloSecao"
                        placeholder="Ex.: A graça que encontra"
                        class="w-full rounded-xl border border-slate-200 px-3.5 py-2 text-sm font-medium text-slate-900 focus:border-slate-400 focus:outline-none"
                      />
                    </div>

                    @if (secoes.length > 1) {
                      <button
                        type="button"
                        (click)="removeSection(i)"
                        class="mt-6 rounded-xl border border-rose-200 p-2 text-rose-600 transition hover:bg-rose-50"
                        title="Remover seção"
                      >
                        <svg lucideTrash2 [size]="16"></svg>
                      </button>
                    }
                  </div>

                  <div>
                    <label class="mb-1.5 block text-xs font-semibold text-slate-700">
                      Conteúdo *
                    </label>
                    <textarea
                      formControlName="texto"
                      rows="12"
                      placeholder="Escreva aqui o texto da seção..."
                      class="w-full rounded-xl border border-slate-200 p-4 text-sm leading-relaxed text-slate-900 placeholder:text-slate-400 focus:border-slate-400 focus:outline-none"
                    ></textarea>
                  </div>
                </div>
              }
            }
          </div>
        </div>
      </form>
    </div>
  `,
})
export class BookEditorComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly bookService = inject(BookService);
  private readonly auth = inject(AuthService);

  readonly editingId = signal<number | null>(null);
  readonly activeSectionIndex = signal(0);

  readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.maxLength(200)]],
    descricao: [''],
    secoes: this.fb.array([this.createSectionGroup('Introdução', '')]),
  });

  get secoes(): FormArray {
    return this.form.controls.secoes;
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      const id = Number(idParam);
      const existing = this.bookService.getLivroById(id);
      if (existing) {
        this.editingId.set(existing.idLivros);
        this.form.patchValue({
          titulo: existing.titulo,
          descricao: existing.descricao || '',
        });
        this.secoes.clear();
        const sections = existing.conteudoSecoes?.length
          ? existing.conteudoSecoes
          : [{ ordem: 1, tituloSecao: 'Capítulo 1', texto: '' }];
        for (const s of sections) {
          this.secoes.push(this.createSectionGroup(s.tituloSecao, s.texto));
        }
      }
    }
  }

  private createSectionGroup(tituloSecao: string, texto: string) {
    return this.fb.nonNullable.group({
      tituloSecao: [tituloSecao, Validators.required],
      texto: [texto, Validators.required],
    });
  }

  addSection(): void {
    const nextNum = this.secoes.length + 1;
    this.secoes.push(this.createSectionGroup(`Seção ${nextNum}`, ''));
    this.activeSectionIndex.set(this.secoes.length - 1);
  }

  removeSection(index: number): void {
    if (this.secoes.length <= 1) return;
    this.secoes.removeAt(index);
    this.activeSectionIndex.update((cur) => Math.max(0, Math.min(cur, this.secoes.length - 1)));
  }

  onSubmit(publicar: boolean): void {
    if (this.form.invalid) return;
    const user = this.auth.currentUser();
    if (!user) return;

    const raw = this.form.getRawValue();
    const saved = this.bookService.saveLivro(user, {
      idLivros: this.editingId() ?? undefined,
      titulo: raw.titulo,
      descricao: raw.descricao,
      publicado: publicar,
      conteudoSecoes: raw.secoes.map((s, idx) => ({
        ordem: idx + 1,
        tituloSecao: s.tituloSecao,
        texto: s.texto,
      })),
    });

    this.router.navigate(['/biblioteca/ler', saved.idLivros]);
  }
}
