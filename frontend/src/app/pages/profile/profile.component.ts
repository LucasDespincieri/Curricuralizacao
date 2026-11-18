import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideChevronRight, LucideEdit3, LucideMapPin } from '@lucide/angular';
import { PostCardComponent } from '../../components/post-card/post-card.component';
import { AuthService } from '../../services/auth.service';
import { BookService } from '../../services/book.service';
import { CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, RouterLink, PostCardComponent, LucideMapPin, LucideEdit3, LucideChevronRight],
  template: `
    @if (auth.currentUser(); as user) {
      <div class="px-6 py-6">
        <!-- Cabeçalho do Perfil -->
        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-xs">
          <div class="flex flex-wrap items-start justify-between gap-4">
            <div class="flex items-center gap-4">
              <div
                class="flex h-16 w-16 shrink-0 items-center justify-center rounded-full border border-slate-300 bg-slate-50 text-lg font-bold text-slate-700"
              >
                {{ getInitials(user.nome) }}
              </div>
              <div>
                <div class="flex items-center gap-2">
                  <h1 class="text-xl font-bold text-slate-900">{{ user.nome }}</h1>
                  <span
                    class="rounded-full px-2.5 py-0.5 text-[11px] font-semibold"
                    [class.bg-slate-800]="user.role === 'PASTOR'"
                    [class.text-white]="user.role === 'PASTOR'"
                    [class.bg-slate-100]="user.role === 'MEMBRO'"
                    [class.text-slate-600]="user.role === 'MEMBRO'"
                  >
                    {{ user.role === 'PASTOR' ? 'Pastor' : 'Membro' }}
                  </span>
                </div>
                <p class="mt-0.5 text-xs text-slate-500">
                  &#64;{{ user.username }} · {{ user.email }}
                </p>
                @if (localizacaoTexto() || user.nascimento) {
                  <p class="mt-1 flex items-center gap-1 text-xs text-slate-500">
                    @if (localizacaoTexto(); as local) {
                      <svg lucideMapPin [size]="13"></svg>
                      <span>{{ local }}</span>
                    }
                    @if (user.nascimento) {
                      <span>{{ localizacaoTexto() ? '· ' : '' }}Nasc.: {{ formatarData(user.nascimento) }}</span>
                    }
                  </p>
                }
              </div>
            </div>

            <button
              type="button"
              (click)="toggleEdit()"
              class="inline-flex items-center gap-1.5 rounded-xl bg-slate-900 px-3.5 py-2 text-xs font-semibold text-white transition hover:bg-slate-800"
            >
              <svg lucideEdit3 [size]="14"></svg>
              <span>{{ editing() ? 'Cancelar' : 'Editar Perfil' }}</span>
            </button>
          </div>

          <!-- Formulário Inline de Edição -->
          @if (editing()) {
            <div class="mt-5 grid gap-3 border-t border-slate-100 pt-5 sm:grid-cols-2">
              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-600">Nome</label>
                <input
                  type="text"
                  [(ngModel)]="editNome"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-600">E-mail</label>
                <input
                  type="email"
                  [(ngModel)]="editEmail"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none"
                />
              </div>
              <div>
                <label class="mb-1 block text-xs font-semibold text-slate-600">Bairro</label>
                <input
                  type="text"
                  [(ngModel)]="editBairro"
                  class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none"
                />
              </div>
              <div class="grid grid-cols-3 gap-2">
                <div class="col-span-2">
                  <label class="mb-1 block text-xs font-semibold text-slate-600">Cidade</label>
                  <input
                    type="text"
                    [(ngModel)]="editCidade"
                    class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs focus:border-slate-400 focus:outline-none"
                  />
                </div>
                <div>
                  <label class="mb-1 block text-xs font-semibold text-slate-600">UF</label>
                  <input
                    type="text"
                    maxlength="2"
                    [(ngModel)]="editUf"
                    class="w-full rounded-xl border border-slate-200 px-3 py-2 text-xs uppercase focus:border-slate-400 focus:outline-none"
                  />
                </div>
              </div>
              <div class="sm:col-span-2 flex justify-end">
                <button
                  type="button"
                  (click)="saveProfile()"
                  class="rounded-xl bg-slate-800 px-4 py-2 text-xs font-semibold text-white hover:bg-slate-900"
                >
                  Salvar Dados
                </button>
              </div>
            </div>
          }

          <!-- Métricas -->
          <div class="mt-6 grid grid-cols-3 divide-x divide-slate-200 rounded-xl bg-slate-50 py-3.5 text-center">
            <div>
              <p class="text-lg font-bold text-slate-900">{{ myPosts().length }}</p>
              <p class="text-[11px] text-slate-500">Posts criados</p>
            </div>
            <div>
              <p class="text-lg font-bold text-slate-900">{{ myBooks().length }}</p>
              <p class="text-[11px] text-slate-500">Livros escritos</p>
            </div>
            <div>
              <p class="text-lg font-bold text-slate-900">{{ bookService.estante().length }}</p>
              <p class="text-[11px] text-slate-500">Livros na Estante</p>
            </div>
          </div>
        </section>

        <!-- Abas do Perfil -->
        <div class="mt-6 flex gap-6 border-b border-slate-200">
          <button
            type="button"
            (click)="activeTab.set('POSTS')"
            class="border-b-2 pb-3 text-sm font-semibold transition"
            [class.border-slate-800]="activeTab() === 'POSTS'"
            [class.text-slate-900]="activeTab() === 'POSTS'"
            [class.border-transparent]="activeTab() !== 'POSTS'"
            [class.text-slate-400]="activeTab() !== 'POSTS'"
          >
            Meus Posts
          </button>

          <button
            type="button"
            (click)="activeTab.set('LIVROS')"
            class="border-b-2 pb-3 text-sm font-semibold transition"
            [class.border-slate-800]="activeTab() === 'LIVROS'"
            [class.text-slate-900]="activeTab() === 'LIVROS'"
            [class.border-transparent]="activeTab() !== 'LIVROS'"
            [class.text-slate-400]="activeTab() !== 'LIVROS'"
          >
            Meus Livros Escritos
          </button>

          <button
            type="button"
            (click)="activeTab.set('ESTANTE')"
            class="border-b-2 pb-3 text-sm font-semibold transition"
            [class.border-slate-800]="activeTab() === 'ESTANTE'"
            [class.text-slate-900]="activeTab() === 'ESTANTE'"
            [class.border-transparent]="activeTab() !== 'ESTANTE'"
            [class.text-slate-400]="activeTab() !== 'ESTANTE'"
          >
            Minha Estante
          </button>
        </div>

        <!-- Conteúdo das Abas -->
        @if (activeTab() === 'POSTS') {
          <div class="-mx-6">
            @for (post of myPosts(); track post.idPost) {
              <app-post-card [post]="post" />
            } @empty {
              <p class="px-6 py-10 text-center text-sm text-slate-500">
                Você ainda não criou nenhuma publicação.
              </p>
            }
          </div>
        }

        @if (activeTab() === 'LIVROS') {
          <div class="mt-5 grid gap-4 sm:grid-cols-2">
            @for (book of myBooks(); track book.idLivros) {
              <article class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-xs">
                <div
                  class="flex h-24 items-end p-4"
                  [style.backgroundColor]="book.corCapa || '#2A4065'"
                >
                  <span class="rounded-full bg-white/90 px-2.5 py-0.5 text-[11px] font-semibold text-slate-800">
                    {{ book.publicado ? 'Publicado' : 'Rascunho' }}
                  </span>
                </div>
                <div class="p-4">
                  <h3 class="text-base font-bold text-slate-900">{{ book.titulo }}</h3>
                  <p class="mt-0.5 text-xs text-slate-400">
                    {{ book.secoes }} seções · {{ book.data_inicio }}
                  </p>
                  <div class="mt-4 flex items-center gap-3">
                    <a
                      [routerLink]="['/biblioteca/editar', book.idLivros]"
                      class="rounded-xl bg-slate-900 px-3.5 py-1.5 text-xs font-semibold text-white hover:bg-slate-800"
                    >
                      Editar Livro
                    </a>
                    <a
                      [routerLink]="['/biblioteca/ler', book.idLivros]"
                      class="inline-flex items-center gap-1 text-xs font-semibold text-slate-700 hover:text-slate-900"
                    >
                      <span>Visualizar</span>
                      <svg lucideChevronRight [size]="14"></svg>
                    </a>
                  </div>
                </div>
              </article>
            } @empty {
              <div class="col-span-full py-10 text-center text-sm text-slate-500">
                Você ainda não escreveu nenhum livro.
                <a routerLink="/biblioteca/novo" class="ml-1 font-semibold text-slate-800 underline">
                  Escrever agora
                </a>
              </div>
            }
          </div>
        }

        @if (activeTab() === 'ESTANTE') {
          <div class="mt-5 grid gap-4 sm:grid-cols-2">
            @for (item of bookService.estante(); track item.idBiblioteca) {
              <article class="rounded-2xl border border-slate-200 bg-white p-4 shadow-xs">
                <h3 class="text-sm font-bold text-slate-900">{{ item.livro.titulo }}</h3>
                <p class="mt-0.5 text-xs text-slate-500">por {{ item.livro.autorNome }}</p>
                <div class="mt-3 flex items-center gap-2">
                  <a
                    [routerLink]="['/biblioteca/ler', item.livro.idLivros]"
                    class="rounded-xl bg-slate-800 px-3 py-1.5 text-xs font-semibold text-white hover:bg-slate-900"
                  >
                    Ler Agora
                  </a>
                </div>
              </article>
            } @empty {
              <p class="col-span-full py-10 text-center text-sm text-slate-500">
                Nenhum livro salvo na estante ainda.
              </p>
            }
          </div>
        }
      </div>
    }
  `,
})
export class ProfileComponent {
  /** "Bairro, Cidade/UF" só com o que o usuário preencheu. */
  protected readonly localizacaoTexto = computed(() => {
    const loc = this.auth.currentUser()?.localizacao;
    if (!loc) return '';
    const cidadeUf = [loc.cidade, loc.uf].filter(Boolean).join('/');
    return [loc.bairro, cidadeUf].filter(Boolean).join(', ');
  });

  protected formatarData(iso: string): string {
    const [ano, mes, dia] = iso.split('-');
    return ano && mes && dia ? `${dia}/${mes}/${ano}` : iso;
  }

  protected readonly auth = inject(AuthService);
  private readonly community = inject(CommunityService);
  protected readonly bookService = inject(BookService);

  readonly activeTab = signal<'POSTS' | 'LIVROS' | 'ESTANTE'>('LIVROS');
  readonly editing = signal(false);

  editNome = '';
  editEmail = '';
  editBairro = '';
  editCidade = '';
  editUf = '';

  readonly myPosts = computed(() => {
    const uid = this.auth.currentUser()?.idUsuario;
    return this.community.posts().filter((p) => p.idUsuario === uid);
  });

  readonly myBooks = computed(() => {
    const uid = this.auth.currentUser()?.idUsuario;
    return this.bookService.livros().filter((l) => l.idUsuario === uid);
  });

  toggleEdit(): void {
    const user = this.auth.currentUser();
    if (!this.editing() && user) {
      this.editNome = user.nome;
      this.editEmail = user.email;
      this.editBairro = user.localizacao.bairro;
      this.editCidade = user.localizacao.cidade;
      this.editUf = user.localizacao.uf;
    }
    this.editing.update((v) => !v);
  }

  saveProfile(): void {
    this.auth.updateProfile({
      nome: this.editNome,
      email: this.editEmail,
      bairro: this.editBairro,
      cidade: this.editCidade,
      uf: this.editUf.toUpperCase(),
    });
    this.editing.set(false);
  }

  getInitials(nome: string): string {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }
}
