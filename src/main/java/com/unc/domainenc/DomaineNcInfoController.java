package com.unc.domainenc;

import com.unc.domainenc.api.DomaineInfoEntity;
import com.unc.domainenc.api.Request;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;


public class DomaineNcInfoController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DomaineNcInfoController.class);
    private final String nom;
    private final Request request;
    private String ridet;
    @FXML
    private VBox infoVbox;

    public DomaineNcInfoController(String nom, Request request) {
        this.nom = nom;
        this.request = request;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        logger.info(String.format("Recuperation des information sur " + nom + ".nc."));
        DomaineInfoEntity domaineInfoEntity = request.getDomaineInfo(nom);
        this.ridet = cleanRidet(domaineInfoEntity.getBeneficiaire());
        infoVbox.getChildren().add(addInfoLien(this.nom + ".nc", FontAwesomeIcon.CHROME));
        infoVbox.getChildren().add(addInfoLienRidet("Bénéficiaire :\n" + ridet, FontAwesomeIcon.CREDIT_CARD));
        infoVbox.getChildren().add(addInfo("Gestionnaire :\n" + domaineInfoEntity.getGestionnaire(), FontAwesomeIcon.USER));
        infoVbox.getChildren().add(addInfo("Date de création :\n" + setDate(domaineInfoEntity.getDateCreation()), FontAwesomeIcon.CHECK_CIRCLE_ALT));
        infoVbox.getChildren().add(addInfo("Date d'expiration :\n" + setDate(domaineInfoEntity.getDateExpiration()), FontAwesomeIcon.CALENDAR_TIMES_ALT));
        infoVbox.getChildren().add(addInfo("Temps avant expiration :\n" + setExpiration(domaineInfoEntity.getNbDaysBeforeExpires()), FontAwesomeIcon.HOURGLASS_END));
        infoVbox.getChildren().add(addDns(domaineInfoEntity.getDns(), FontAwesomeIcon.SERVER));
    }

    public HBox addInfo(String contenu, FontAwesomeIcon iconType) {
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("28px");
        icon.getStyleClass().add("color");
        Label info = new Label(contenu);
        info.getStyleClass().add("color");
        HBox container = new HBox(16);
        container.setPadding(new Insets(8));
        container.setPrefSize(300, 60);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(icon, info);
        return container;
    }

    public HBox addInfoLien(String contenu, FontAwesomeIcon iconType) {
        HBox container = addInfo(contenu, iconType);
        container.setOnMouseClicked(this::openDomaineNc);
        container.setOnMouseEntered(e -> container.setCursor(Cursor.HAND));
        container.setOnMouseExited(e -> container.setCursor(Cursor.DEFAULT));
        return container;
    }

    public HBox addInfoLienRidet(String contenu, FontAwesomeIcon iconType) {
        HBox container = addInfo(contenu, iconType);
        if (this.ridet.startsWith("Ridet :")) {
            container.setOnMouseClicked(this::openRidetEntreprise);
            container.setOnMouseEntered(e -> container.setCursor(Cursor.HAND));
            container.setOnMouseExited(e -> container.setCursor(Cursor.DEFAULT));
        }
        return container;
    }

    public HBox addDns(List<String> dnsList, FontAwesomeIcon iconType) {
        ObservableList<String> data = FXCollections.observableArrayList(dnsList);
        FontAwesomeIconView icon = new FontAwesomeIconView(iconType);
        icon.setSize("28px");
        icon.getStyleClass().add("color");
        ListView<String> listView = new ListView<>(data);
        Label info = new Label("Serveur DNS :");
        info.getStyleClass().add("color");
        VBox vbox = new VBox();
        vbox.getChildren().addAll(info, listView);
        HBox container = new HBox(16);
        container.setPadding(new Insets(8));
        container.setPrefSize(300, 80);
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(icon, vbox);
        return container;
    }

    public String setDate(String stringDate) {
        try {
            SimpleDateFormat ymdFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat dmyFormat = new SimpleDateFormat("dd MMMM yyyy");
            Date date = ymdFormat.parse(stringDate);
            return dmyFormat.format(date);
        } catch (ParseException dateException) {
            logger.error("Erreur parsing date");
        }
        return "";
    }

    public String setExpiration(int joursRestant) {
        String res;
        if (joursRestant > 365) {
            res = joursRestant / 365 + " années restantes";
        } else if (joursRestant > 31) {
            res = joursRestant / 31 + " mois restants";
        } else {
            res = joursRestant + " jours restants";
        }
        return res;
    }

    public String cleanRidet(String beneficiaire) {
        if (beneficiaire.startsWith("Ridet : ")) {
            int index = beneficiaire.indexOf("-", "Ridet : ".length());
            if (index != -1) {
                beneficiaire = beneficiaire.substring(0, index);
            }
        }
        return beneficiaire;
    }

    public void openDomaineNc(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                DomaineNcApp.browse("https://www.domaine.nc/whos?domain=" + this.nom + "&ext=.nc");
            }
        }
    }

    public void openRidetEntreprise(MouseEvent event) {
        if (event.getButton().equals(MouseButton.PRIMARY)) {
            if (event.getClickCount() == 2) {
                String[] num = this.ridet.split(" ");
                DomaineNcApp.browse("https://data.gouv.nc/explore/dataset/entreprises-actives-au-ridet/table/" +
                        "?disjunctive.libelle_formjur&disjunctive.code_ape&disjunctive.libelle_naf&disjunctive.section_naf" +
                        "&disjunctive.libelle_section_naf&disjunctive.libelle_commune&disjunctive.hors_nc&disjunctive.province&q=" + num[2]);
            }
        }
    }
}
