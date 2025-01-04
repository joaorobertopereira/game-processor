package bet.logame.app.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Entity
@Table(name = "sis_cassino_jogos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SisCassinoJogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "`insert`")
    private Timestamp insert;

    @Column(name = "`update`")
    private Timestamp update;

    @Column(name = "token")
    private String token;

    @Column(name = "status")
    private Integer status;

    @Column(name = "fornecedor")
    private String fornecedor;

    @Column(name = "provedor")
    private String provedor;

    @Column(name = "gameid")
    private String gameid;

    @Column(name = "nome")
    private String nome;

    @Column(name = "type")
    private String type;

    @Column(name = "demo")
    private Integer demo;

    @Column(name = "mobile")
    private Integer mobile;

    @Column(name = "aovivo")
    private Integer aovivo;

    @Column(name = "lobby")
    private Integer lobby;

    @Column(name = "aspectratio")
    private String aspectratio;

    @Column(name = "imagem_horizontal")
    private String imagemHorizontal;

    @Column(name = "imagem_quadrada")
    private String imagemQuadrada;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "type_import")
    private String typeImport;

    @Column(name = "color")
    private String color;

    @Column(name = "tag_color")
    private String tagColor;

    @Column(name = "tag_type")
    private String tagType;

    @Column(name = "tema")
    private String tema;
}
