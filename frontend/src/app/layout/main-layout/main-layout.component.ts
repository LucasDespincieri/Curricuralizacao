import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { LucideMenu, LucideX } from '@lucide/angular';
import { SidebarComponent } from '../../components/sidebar/sidebar.component';
import { RightWidgetsComponent } from '../../components/right-widgets/right-widgets.component';
import { LoginPromptComponent } from '../../components/login-prompt/login-prompt.component';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    RouterLink,
    SidebarComponent,
    RightWidgetsComponent,
    LoginPromptComponent,
    LucideMenu,
    LucideX,
  ],
  template: `
    <div class="min-h-screen bg-[#FAFAFA] text-slate-900">
      <!-- Header Mobile -->
      <header
        class="sticky top-0 z-30 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3 lg:hidden"
      >
        <div class="flex items-center gap-2.5">
          <span
            class="flex h-8 w-8 items-center justify-center rounded-lg bg-slate-800 text-xs font-bold text-white"
          >
            B!
          </span>
          <span class="font-bold text-slate-900">Comunidade DA FÉ</span>
        </div>
        <div class="flex items-center gap-2">
        @if (!auth.currentUser()) {
          <a
            routerLink="/login"
            class="rounded-full bg-slate-800 px-3.5 py-1.5 text-xs font-semibold text-white transition hover:bg-slate-900"
          >
            Entrar
          </a>
        }
        <button
          type="button"
          (click)="mobileMenuOpen.set(!mobileMenuOpen())"
          class="rounded-lg p-2 text-slate-600 hover:bg-slate-100"
          aria-label="Abrir menu"
        >
          @if (mobileMenuOpen()) {
            <svg lucideX [size]="20"></svg>
          } @else {
            <svg lucideMenu [size]="20"></svg>
          }
        </button>
        </div>
      </header>

      <!-- Mobile Drawer -->
      @if (mobileMenuOpen()) {
        <div
          class="fixed inset-0 z-40 bg-slate-900/40 lg:hidden"
          (click)="mobileMenuOpen.set(false)"
        >
          <div class="h-full w-64 bg-white" (click)="$event.stopPropagation()">
            <app-sidebar (newPostClicked)="mobileMenuOpen.set(false)" />
          </div>
        </div>
      }

      <!-- Grid de 3 Colunas -->
      <div class="mx-auto flex min-h-screen max-w-[1280px]">
        <!-- Coluna Esquerda (Sidebar Fixa ~250px) -->
        <div class="hidden w-[250px] shrink-0 lg:block">
          <div class="sticky top-0 h-screen">
            <app-sidebar />
          </div>
        </div>

        <!-- Coluna Central (Área Dinâmica) -->
        <main
          class="min-w-0 flex-1 border-r border-slate-200 bg-white"
          [class.max-w-[680px]]="!isWideView()"
        >
          <router-outlet />
        </main>

        <!-- Coluna Direita (Widgets Contextuais ~300px, oculta no Leitor Focado e em telas menores) -->
        @if (!isReaderMode()) {
          <aside class="hidden w-[320px] shrink-0 px-6 py-6 xl:block">
            <div class="sticky top-6">
              <app-right-widgets />
            </div>
          </aside>
        }
      </div>

      <!-- Pop-up "Entre para continuar" (aberto pelo LoginPromptService) -->
      <app-login-prompt />
    </div>
  `,
})
export class MainLayoutComponent {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  readonly mobileMenuOpen = signal(false);
  private readonly currentUrl = signal(this.router.url);

  readonly isReaderMode = computed(() => this.currentUrl().includes('/biblioteca/ler/'));
  readonly isWideView = computed(() => this.isReaderMode());

  constructor() {
    this.router.events
      .pipe(filter((e): e is NavigationEnd => e instanceof NavigationEnd))
      .subscribe((e) => {
        this.currentUrl.set(e.urlAfterRedirects);
        this.mobileMenuOpen.set(false);
      });
  }
}
