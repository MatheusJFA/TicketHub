import { describe, expect, it, vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { AuthService } from '../../core/auth.service';

function setup(loginImpl: () => ReturnType<AuthService['login']>) {
  const login = vi.fn(loginImpl);
  const navigateByUrl = vi.fn();
  TestBed.configureTestingModule({
    imports: [LoginComponent],
    providers: [
      { provide: AuthService, useValue: { login } },
      { provide: Router, useValue: { navigateByUrl } },
      {
        provide: ActivatedRoute,
        useValue: { snapshot: { queryParamMap: { get: () => null } } },
      },
    ],
  });
  const fixture = TestBed.createComponent(LoginComponent);
  fixture.detectChanges();
  return { fixture, component: fixture.componentInstance, login, navigateByUrl };
}

describe('LoginComponent', () => {
  it('cria com formulário inválido e botão desabilitado', () => {
    const { component, fixture } = setup(() => of({}) as never);
    expect(component.form.invalid).toBe(true);
    fixture.detectChanges();
    const btn = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(btn.disabled).toBe(true);
  });

  it('não submete quando inválido', () => {
    const { component, login } = setup(() => of({}) as never);
    component.submit();
    expect(login).not.toHaveBeenCalled();
  });

  it('loga e navega para / ao sucesso', () => {
    const { component, login, navigateByUrl } = setup(() => of({}) as never);
    component.form.setValue({ identifier: 'user@x.com', password: 'secreta1' });
    component.submit();
    expect(login).toHaveBeenCalledWith('user@x.com', 'secreta1');
    expect(navigateByUrl).toHaveBeenCalledWith('/');
    expect(component.loading()).toBe(false);
  });

  it('exibe erro da API ao falhar', () => {
    const { component } = setup(
      () => throwError(() => ({ error: { errors: [{ message: 'Ops' }] } })) as never,
    );
    component.form.setValue({ identifier: 'user@x.com', password: 'secreta1' });
    component.submit();
    expect(component.error()).toBe('Ops');
    expect(component.loading()).toBe(false);
  });
});
