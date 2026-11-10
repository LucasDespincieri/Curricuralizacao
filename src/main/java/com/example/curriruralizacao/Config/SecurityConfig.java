package com.example.curriruralizacao.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

// @Configuration: Marca a classe como uma fonte de definições de beans gerenciados pelo contexto do Spring.
@Configuration
// @EnableWebSecurity: Habilita os recursos e filtros de segurança do Spring Security na aplicação web.
@EnableWebSecurity
// @EnableMethodSecurity: Permite a utilização de anotações de segurança diretamente nos métodos dos controllers ou services (ex: @PreAuthorize).
@EnableMethodSecurity
public class SecurityConfig {

    // Define o Bean responsável pela criptografia e conferência de senhas dos usuários.
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder: Utiliza o algoritmo BCrypt com salt aleatório para garantir armazenamento seguro de senhas no banco.
        return new BCryptPasswordEncoder();
    }

    // Configura a cadeia de filtros de segurança (Security Filter Chain) da aplicação HTTP.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Configuração das regras de autorização de requisições HTTP:
                .authorizeHttpRequests(authorize -> authorize
                        // Recursos estáticos (estilos CSS, scripts JS, imagens e ícone) liberados para qualquer visitante.
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        // Telas públicas principais: página inicial, feed geral, tela de login e página de cadastro liberadas para todos.
                        .requestMatchers("/", "/feed", "/login", "/cadastro").permitAll()
                        // Catálogo público de livros e rota de leitura de capítulos liberados para visitação pública.
                        .requestMatchers("/biblioteca", "/livros/*/ler").permitAll()
                        // Rotas administrativas iniciadas por /admin/ restritas exclusivamente a usuários com papel ADM (Pastor/Admin).
                        .requestMatchers("/admin/**").hasRole("ADM")
                        // Qualquer outra requisição não listada acima exige que o usuário esteja devidamente autenticado (sessão ativa).
                        .anyRequest().authenticated()
                )
                // Configuração da autenticação via formulário de login (Form Login):
                .formLogin(form -> form
                        // URL customizada que renderiza a página de login.
                        .loginPage("/login")
                        // Endpoint processador que recebe a submissão POST do formulário de login (email e senha).
                        .loginProcessingUrl("/login")
                        // Rota de redirecionamento após autenticação bem-sucedida (forçando ida para o /feed).
                        .defaultSuccessUrl("/feed", true)
                        // URL de retorno com flag de erro caso as credenciais sejam inválidas.
                        .failureUrl("/login?error=true")
                        // Permite acesso irrestrito aos endpoints de login.
                        .permitAll()
                )
                // Configuração de encerramento de sessão (Logout):
                .logout(logout -> logout
                        // URL que dispara a ação de logout via POST/GET.
                        .logoutUrl("/logout")
                        // Rota de redirecionamento após sair da conta (retorna à tela de login com flag indicativa).
                        .logoutSuccessUrl("/login?logout=true")
                        // Invalida a sessão HTTP no servidor para revogar o acesso imediatamente.
                        .invalidateHttpSession(true)
                        // Remove o cookie de sessão 'JSESSIONID' do navegador do usuário.
                        .deleteCookies("JSESSIONID")
                        // Permite que qualquer usuário acione o logout.
                        .permitAll()
                );

        // Constrói e retorna a cadeia de filtros de segurança configurada.
        return http.build();
    }
}
