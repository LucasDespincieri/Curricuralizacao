import { Routes } from '@angular/router';
import { adminGuard } from './guards/admin.guard';
import { exigeLoginGuard } from './guards/auth.guard';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';
import { AdminPageComponent } from './pages/admin/admin-page.component';
import { AuthPageComponent } from './pages/auth/auth-page.component';
import { BookEditorComponent } from './pages/editor/book-editor.component';
import { FeedPageComponent } from './pages/feed/feed-page.component';
import { BookListComponent } from './pages/library/book-list.component';
import { BookReaderComponent } from './pages/library/book-reader.component';
import { ProfileComponent } from './pages/profile/profile.component';
import { ShelfComponent } from './pages/shelf/shelf.component';

export const routes: Routes = [
  { path: 'login', component: AuthPageComponent },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'inicio', pathMatch: 'full' },
      { path: 'inicio', component: FeedPageComponent },
      { path: 'biblioteca', component: BookListComponent },
      { path: 'biblioteca/novo', component: BookEditorComponent, canActivate: [exigeLoginGuard] },
      { path: 'biblioteca/editar/:id', component: BookEditorComponent, canActivate: [exigeLoginGuard] },
      { path: 'biblioteca/ler/:id', component: BookReaderComponent },
      { path: 'estante', component: ShelfComponent, canActivate: [exigeLoginGuard] },
      { path: 'perfil', component: ProfileComponent, canActivate: [exigeLoginGuard] },
      { path: 'admin', component: AdminPageComponent, canActivate: [adminGuard] },
      { path: '**', redirectTo: 'inicio' },
    ],
  },
];
