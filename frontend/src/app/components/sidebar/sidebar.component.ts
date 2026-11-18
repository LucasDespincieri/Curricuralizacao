import { Component, inject, output } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import {
  LucideBookmark,
  LucideBookOpen,
  LucideHome,
  LucideLogIn,
  LucideLogOut,
  LucidePenLine,
  LucideSparkles,
  LucideUser,
} from '@lucide/angular';
import { AuthService } from '../../services/auth.service';
import { LoginPromptService } from '../../services/login-prompt.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    LucideHome,
    LucideBookOpen,
    LucideBookmark,
    LucideUser,
    LucidePenLine,
    LucideLogIn,
    LucideLogOut,
    LucideSparkles,
  ],
  template: `
    <aside
      class="flex h-full w-full flex-col justify-between border-r border-slate-200 bg-white px-4 py-6"
    >
      <div>
        <!-- Logo -->
        <a routerLink="/inicio" class="mb-8 flex items-center gap-3 px-2">
          <div
            class="flex h-10 w-10 items-center justify-center rounded-xl bg-slate-800 text-white shadow-sm"
          >
            <svg lucideSparkles [size]="20"></svg>
          </div>
          <div class="leading-tight">
            <div class="text-base font-bold tracking-tight text-slate-900">Comunidade</div>
            <div class="text-[11px] font-medium tracking-widest text-slate-500 uppercase">
              DA FÉ · BASE!
            </div>
          </div>
        </a>

        <!-- Navegação Principal (4 rotas do MVP) -->
        <nav class="space-y-1.5" aria-label="Navegação Principal">
          <a
            routerLink="/inicio"
            routerLinkActive="bg-slate-100 text-slate-900 font-semibold"
            class="flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
          >
            <svg lucideHome [size]="18"></svg>
            <span>Início</span>
          </a>

          <a
            routerLink="/biblioteca"
            routerLinkActive="bg-slate-100 text-slate-900 font-semibold"
            class="flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
          >
            <svg lucideBookOpen [size]="18"></svg>
            <span>Biblioteca de Livros</span>
          </a>

          <a
            routerLink="/estante"
            routerLinkActive="bg-slate-100 text-slate-900 font-semibold"
            class="flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
          >
            <svg lucideBookmark [size]="18"></svg>
            <span>Minha Estante</span>
          </a>

          <a
            routerLink="/perfil"
            routerLinkActive="bg-slate-100 text-slate-900 font-semibold"
            class="flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium text-slate-600 transition-colors hover:bg-slate-50 hover:text-slate-900"
          >
            <svg lucideUser [size]="18"></svg>
            <span>Perfil</span>
          </a>
        </nav>

        <!-- Botão de ação primária -->
        <button
          type="button"
          (click)="onNovoPost()"
          class="mt-6 flex w-full items-center justify-center gap-2 rounded-full bg-slate-800 px-4 py-2.5 text-sm font-semibold text-white shadow-sm transition hover:bg-slate-900 active:scale-[0.99]"
        >
          <svg lucidePenLine [size]="16"></svg>
          <span>Novo Post</span>
        </button>
      </div>

      <!-- Rodapé: Mini-card do usuário -->
      @if (auth.currentUser(); as user) {
        <div
          class="mt-6 flex items-center justify-between gap-2 rounded-xl border border-slate-200 bg-slate-50/70 p-2.5"
        >
          <a routerLink="/perfil" class="flex min-w-0 items-center gap-2.5">
            <div
              class="flex h-9 w-9 shrink-0 items-center justify-center rounded-full border border-slate-300 bg-white text-xs font-semibold text-slate-700"
            >
              {{ getInitials(user.nome) }}
            </div>
            <div class="min-w-0">
              <p class="truncate text-sm font-semibold text-slate-900">{{ user.nome }}</p>
              <p class="truncate text-xs text-slate-500">&#64;{{ user.username }}</p>
            </div>
          </a>

          <button
            type="button"
            (click)="onLogout()"
            title="Sair"
            aria-label="Sair"
            class="rounded-lg p-1.5 text-slate-400 transition hover:bg-slate-200/70 hover:text-slate-700"
          >
            <svg lucideLogOut [size]="16"></svg>
          </button>
        </div>
      } @else {
        <!-- Visitante sem login: convite para entrar -->
        <div class="mt-6 rounded-xl border border-slate-200 bg-slate-50/70 p-3.5">
          <p class="text-sm font-semibold text-slate-900">Faça parte da comunidade</p>
          <p class="mt-0.5 text-xs leading-relaxed text-slate-500">
            Entre para publicar, comentar e salvar livros na sua estante.
          </p>
          <a
            routerLink="/login"
            class="mt-3 flex w-full items-center justify-center gap-2 rounded-full bg-slate-800 px-4 py-2 text-sm font-semibold text-white transition hover:bg-slate-900"
          >
            <svg lucideLogIn [size]="15"></svg>
            <span>Entrar</span>
          </a>
          <a
            routerLink="/login"
            [queryParams]="{ modo: 'cadastro' }"
            class="mt-2 flex w-full items-center justify-center rounded-full border border-slate-300 px-4 py-2 text-sm font-semibold text-slate-800 transition hover:bg-white"
          >
            Criar conta
          </a>
        </div>
      }
    </aside>
  `,
})
export class SidebarComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly loginPrompt = inject(LoginPromptService);
  readonly newPostClicked = output<void>();

  getInitials(nome: string): string {
    return nome
      .split(' ')
      .filter(Boolean)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  onNovoPost(): void {
    this.newPostClicked.emit();
    if (!this.loginPrompt.exigirLogin('Entre na sua conta para publicar no feed da comunidade.')) return;
    this.router.navigate(['/inicio'], { queryParams: { novo: '1' } });
  }

  onLogout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
