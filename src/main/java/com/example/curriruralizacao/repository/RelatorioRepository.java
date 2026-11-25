package com.example.curriruralizacao.repository;

import com.example.curriruralizacao.DTO.RelatorioAgrupamentoDTO;
import com.example.curriruralizacao.DTO.RelatorioPivotDTO;
import com.example.curriruralizacao.DTO.RelatorioTotalizacaoDTO;
import com.example.curriruralizacao.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

// 1. Consulta em múltiplas tabelas com totalização
// 2. Consulta de agrupamento (GROUP BY + HAVING)
// 3. Consulta de Pivot (Crosstab).
@Repository
public interface RelatorioRepository extends JpaRepository<Usuario, Integer> {

    // Une (LEFT JOIN) 4 tabelas: Usuario, Post, Comentarios e ReacaoPost
    // Totaliza com funções de agregação (COUNT(DISTINCT ...)) posts, comentários e reações por usuário.
    @Query("SELECT new com.example.curriruralizacao.DTO.RelatorioTotalizacaoDTO(" +
           "u.id, u.nome, u.email, " +
           "COUNT(DISTINCT p.id), COUNT(DISTINCT c.id), COUNT(DISTINCT r.id)) " +
           "FROM Usuario u " +
           "LEFT JOIN u.posts p " +
           "LEFT JOIN u.comentarios c " +
           "LEFT JOIN u.reacoes r " +
           "GROUP BY u.id, u.nome, u.email " +
           "ORDER BY COUNT(DISTINCT p.id) DESC, COUNT(DISTINCT c.id) DESC")

    List<RelatorioTotalizacaoDTO> relatorioTotalizacaoMultiplasTabelas();

    // Agrupa dados com cláusula GROUP BY e filtra grupos agregados com HAVING
    // Calcula o total de livros e capítulos por autor cadastrado.
    @Query("SELECT new com.example.curriruralizacao.DTO.RelatorioAgrupamentoDTO(" +
           "u.nome, COUNT(DISTINCT l.id), COUNT(DISTINCT cap.id)) " +
           "FROM Livro l " +
           "JOIN l.autor u " +
           "LEFT JOIN l.capitulos cap " +
           "GROUP BY u.id, u.nome " +
           "HAVING COUNT(DISTINCT l.id) > 0 " +
           "ORDER BY COUNT(DISTINCT l.id) DESC")
    List<RelatorioAgrupamentoDTO> relatorioAgrupamentoAutoresLivros();

    // Realiza uma consulta Crosstab/Pivot transformando as linhas da tabela de reações ('AMEM', 'CURTIR', 'ORANDO')
    // em colunas distintas através da agregação condicional SUM(CASE WHEN ... THEN 1L ELSE 0L END),
    // além de somar o total consolidado de reações recebidas por cada publicação.
    @Query("SELECT new com.example.curriruralizacao.DTO.RelatorioPivotDTO(" +
           "p.id, " +
           "p.conteudo, " +
           "u.nome, " +
           "SUM(CASE WHEN UPPER(r.tipo) = 'AMEM' THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN UPPER(r.tipo) = 'CURTIR' THEN 1L ELSE 0L END), " +
           "SUM(CASE WHEN UPPER(r.tipo) = 'ORANDO' THEN 1L ELSE 0L END), " +
           "COUNT(r.id)) " +
           "FROM Post p " +
           "JOIN p.autor u " +
           "LEFT JOIN p.reacoes r " +
           "GROUP BY p.id, p.conteudo, u.nome " +
           "ORDER BY COUNT(r.id) DESC, p.id DESC")
    List<RelatorioPivotDTO> relatorioPivotReacoesPorPost();

}
