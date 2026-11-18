import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Protege páginas que só fazem sentido com login (Estante, Perfil, editor de livros).
 * Visitante sem login é levado para /login e, depois de entrar, volta para a página que tentou abrir.
 */
export const exigeLoginGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.currentUser()) return true;
  return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
};
