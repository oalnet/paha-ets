package net.oal.ets.planhoraire.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.*;

import java.util.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Planhoraire implements EntryPoint {

	CoursServiceAsync coursService = null;

	Panel betaPanel = new StackPanel();
	Panel statusPanel = new StackPanel();
	Panel errorPanel = new StackPanel();

	HorizontalPanel horaireSelectionPanel = new HorizontalPanel();
	Panel synthesePanel = new HorizontalPanel();
	Panel grillePanel = new VerticalPanel();
	Panel selectionPanel = new VerticalPanel();
	Panel indispoPanel = new HorizontalPanel();

	Map listeHoraires = new HashMap();

	List listeCours = new ArrayList();
	Planificateur planificateur = null;

	private void initServices() {
		coursService =	(CoursServiceAsync) GWT.create(CoursService.class);
		ServiceDefTarget target = (ServiceDefTarget) coursService;
		String relativeUrl = GWT.getModuleBaseURL() + "cours.do";
//		relativeUrl = "/planhoraire/cours.do";
		target.setServiceEntryPoint(relativeUrl);
	}

	private void initData() {
		this.initServices();
		planificateur = new Planificateur();
		this.callForHoraires();
	}

	public void callForHoraires() {
		this.setStatus("Récuperation de la liste des horaires...");
		coursService.getListeHoraireId(new AsyncCallback(){
			public void onFailure(Throwable caught) {
				logError(caught);
			}
			public void onSuccess(Object result) {
				listeHoraires = (Map) result;
				setStatus("Récuperation de la liste des horaires terminée.");
				refreshHoraireSelectionPanel();
			}
		});
	}

	public void callForCours(final String horaireSelected) {
		if(horaireSelected != null) {
			this.setStatus("Récuperation de la liste des cours pour l'horaire '"+horaireSelected+"'...");
			coursService.getListeCours(horaireSelected, new AsyncCallback(){
				public void onFailure(Throwable caught) {
					logError(caught);
				}
				public void onSuccess(Object result) {
					listeCours = (List) result;
					Collections.sort(listeCours, new CoursComparator());
//					planificateur.updateConflict(listeCours);
					setStatus("Récuperation de la liste des cours pour l'horaire '"+horaireSelected+"' terminée.");
					callForUpdateConflicts();
				}
			});
		}
	}

	protected Cours getCours(String coursId) {
		this.setStatus("Recherche du Cours '"+coursId+"' dans l'horaire...");
		for(Iterator iter = this.listeCours.iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			if(cours.getId().equals(coursId.toUpperCase())) {
				return cours;
			}
		}
		this.setStatus("Cours '"+coursId+"' non présent dans l'horaire...");
		return null;
	}
	protected void addCours(String coursId) {
		Cours cours = this.getCours(coursId);
		if(cours == null) {
			this.setStatus("Cours '"+cours.getId()+"' non présent dans l'horaire...");
		} else if(planificateur.getCoursChoisis().contains(cours)) {
			this.setStatus("Cours '"+cours.getId()+"' déjà pris en compte dans la planification...");
		} else {
			this.addCours(cours);
		}
	}
	protected void addCours(Cours cours) {
		this.setStatus("Ajout du cours '"+cours.getId()+"' à la planification...");
		this.planificateur.add(cours);
		this.setStatus("Cours '"+cours.getId()+"' ajouté à la planification.");
		this.callForCalculate();
	}

	protected void addIndispo(Indisponibilite indisp) {
		this.setStatus("Ajout de l'indisponibilité '"+indisp.toString()+"' à la planification...");
		this.planificateur.add(indisp);
		this.setStatus("Indisponibilité '"+indisp.toString()+"' ajoutée à la planification.");
		this.callForCalculate();
	}
	protected void removeIndispo(Indisponibilite indisp) {
		this.setStatus("Retrait de l'indisponibilité '"+indisp.toString()+"' à la planification...");
		this.planificateur.remove(indisp);
		this.setStatus("Indisponibilité '"+indisp.toString()+"' ajoutée à la planification.");
		this.callForCalculate();
	}

	public void callForCalculate() {
		if(planificateur != null) {
			this.setStatus("Planification en cours...");
			coursService.calculateGrilles(planificateur, new AsyncCallback(){
				public void onFailure(Throwable caught) {
					logError(caught);
				}
				public void onSuccess(Object result) {
					planificateur = (Planificateur) result;
					callForUpdateConflicts();
				}
			});
		}
	}

	public void callForUpdateConflicts() {
		if(planificateur != null) {
			this.setStatus("Planification en cours...");
			coursService.updateConflict(planificateur, listeCours, new AsyncCallback(){
				public void onFailure(Throwable caught) {
					logError(caught);
				}
				public void onSuccess(Object result) {
					planificateur = (Planificateur) result;
					refreshListe();
				}
			});
		}
	}

	protected void refreshListe() {
//		this.setStatus("Mise à jour des grilles horaires possibles.");
//		this.planificateur.calculateGrilles();
//		this.setStatus("Mise à jour de la liste des cours conflictuels.");
//		this.planificateur.updateConflict(this.listeCours);
		this.setStatus("Mise à jour de la table de sélection.");
		this.refreshSelectionPanel();
		this.setStatus("Mise à jour de la grille de synthèse.");
		this.refreshSynthesePanel();
		this.setStatus("Mise à jour des grilles horaire.");
		this.refreshGrillePanel();
		this.refreshGrilleIndispo();
	}
	protected void removeCours(Cours cours) {
		this.setStatus("Retrait du cours '"+cours.getId()+"' de la planification...");
		this.planificateur.remove(cours);
		this.setStatus("Cours '"+cours.getId()+"' retiré de la planification.");
		this.callForCalculate();
	}

	protected void setStatus(String status) {
		statusPanel.clear();
		statusPanel.add(new Label(status));
	}

	protected void logError(String error) {
		errorPanel.clear();
		errorPanel.add(new Label(error));
	}
	protected void logError(Throwable ex) {
		errorPanel.clear();
		errorPanel.add(new HTML(ex.getLocalizedMessage()));
//		errorPanel.setVisible(false);
	}

	/**
   * This is the entry point method.
   */
	public void onModuleLoad() {
		this.initData();
	    RootPanel.get("title").addStyleName("title");
	    RootPanel.get("title").add(new HTML("Planificateur d'horaire (Beta)"));
		DockPanel rootPanel = new com.google.gwt.user.client.ui.DockPanel();
		rootPanel.setWidth("100%");
		rootPanel.setSpacing(5);

		HTML betaLabel = new HTML("<div class=\"betabox\"><font color=\"red\"><h3>Cet outils est actuellement en version Beta</h3></font>Les données de l'outils étant extraites du fichier PDF, il peut arriver que certaines de ces données ne soient pas tout à fait exactes.<br/>Veuillez vous référer au <a href=\"http://www.etsmtl.ca/zone2/programmes/Horaire/index.html\" target=\"blank_\">fichier PDF de l'horaire</a> pour vérifier la validité de l'horaire généré.<br/>De plus, les contraintes imposées par le cheminot ne sont pas prises en charge par l'outils. Il est par conséquent recommandé que vous généreriez plusieurs horaires possibles</p>");

		VerticalPanel southPanel = new VerticalPanel();
		southPanel.setWidth("100%");
		betaPanel.addStyleName("messagebox");
		betaPanel.setWidth("100%");
		southPanel.add(betaLabel);
		southPanel.add(new Label());
		statusPanel.addStyleName("messagebox");
		statusPanel.setWidth("100%");
		southPanel.add(statusPanel);
		errorPanel.addStyleName("messagebox-error");
		errorPanel.setWidth("100%");
		southPanel.add(errorPanel);

		southPanel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
		southPanel.add(horaireSelectionPanel);
		southPanel.add(new HTML("<hr/>"));
		refreshHoraireSelectionPanel();

		rootPanel.add(southPanel, DockPanel.NORTH);

		HorizontalPanel centerPanel = new HorizontalPanel();
		centerPanel.setWidth("100%");
		centerPanel.setSpacing(5);
		centerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
		rootPanel.add(centerPanel, DockPanel.CENTER);

		VerticalPanel c1Panel = new VerticalPanel();
		this.refreshSynthesePanel();
		c1Panel.add(synthesePanel);
		this.refreshGrillePanel();
		c1Panel.add(grillePanel);

		centerPanel.add(c1Panel);

		this.refreshSelectionPanel();
		centerPanel.add(selectionPanel);

		RootPanel.get("content").add(rootPanel);
	}
	
	protected void refreshHoraireSelectionPanel() {
		horaireSelectionPanel.clear();
//		horaireSelectionPanel.setWidth("100%");
		horaireSelectionPanel.setSpacing(3);
		horaireSelectionPanel.add(new Label("Horaire :"));
		ListBox horaireSelect = new ListBox();
		horaireSelect.addItem("Sélectionnez un horaire...");
		for(Iterator iter = this.listeHoraires.keySet().iterator(); iter.hasNext();) {
			String horaireId = (String) iter.next();
			String horaireLabel = (String) listeHoraires.get(horaireId);
			horaireSelect.addItem(horaireLabel);
			horaireSelect.setValue(horaireSelect.getItemCount()-1, horaireId);
		}
		horaireSelect.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender) {
				ListBox horaireSelect = (ListBox) sender;
				if(horaireSelect.getSelectedIndex() > 0) {
					String horaireSelected = horaireSelect.getValue(horaireSelect.getSelectedIndex());
					callForCours(horaireSelected);
				}
			}
		});
		horaireSelectionPanel.add(horaireSelect);
	}
	
	protected void refreshSynthesePanel() {
		synthesePanel.clear();
//		synthesePanel.setWidth("100%");

		FlexTable synt = new FlexTable();
		synt.addStyleName("body");
		synt.setBorderWidth(1);
		int currentRow = synt.getRowCount();
		synt.getRowFormatter().addStyleName(currentRow, "tableheader");
		synt.getFlexCellFormatter().setColSpan(currentRow, 0, 2);
		synt.setText(currentRow, 0, "Modification rapide");
		synt.getCellFormatter().addStyleName(currentRow, 0, "tableheader");

		currentRow = synt.getRowCount();
		synt.getRowFormatter().addStyleName(currentRow, "body");
		TextBox tb = new TextBox();
		tb.setMaxLength(7);
		tb.setWidth("60");
		tb.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender) {
				TextBox tb = (TextBox) sender;
				addCours(tb.getText());
			}
		});
		synt.setWidget(currentRow, 0, tb);
		synt.getCellFormatter().addStyleName(currentRow, 0, "body");
		ListBox lb = new ListBox();
		lb.setWidth("300");
		lb.addItem("Sélectionnez un cours à ajouter...");
		for(Iterator iter = planificateur.getCoursSansConflits().iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			lb.addItem(cours.getId()+" - "+cours.getDescription());
			lb.setValue(lb.getItemCount()-1, cours.getId());
		}
		lb.addChangeListener(new ChangeListener(){
			public void onChange(Widget sender) {
				ListBox lb = (ListBox) sender;
				if(lb.getSelectedIndex() > 0) {
					String coursId = lb.getValue(lb.getSelectedIndex());
					addCours(coursId);
				}
			}
		});
		synt.setWidget(currentRow, 1, lb);
		synt.getCellFormatter().addStyleName(currentRow, 1, "body");

		currentRow = synt.getRowCount();
		synt.getRowFormatter().addStyleName(currentRow, "tableheader");
		synt.setText(currentRow, 0, "Bloquer des plages");
		synt.getCellFormatter().addStyleName(currentRow, 0, "tableheader");
		synt.setText(currentRow, 1, "Synthèse");
		synt.getCellFormatter().addStyleName(currentRow, 1, "tableheader");

		FlexTable tab = new FlexTable();
		tab.setBorderWidth(1);
		int currentcours = 1;
		tab.getColumnFormatter().setWidth(0, "5%");
		tab.getColumnFormatter().setWidth(1, "10%");
		for(Iterator iter = this.planificateur.getCoursChoisis().iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			currentRow = tab.getRowCount();
			tab.addTableListener(new RowObjectTableListener(cours, currentRow) {
				protected void doAction(Object cours) {
					removeCours((Cours) cours);
				}
			});
			tab.setText(currentRow, 0, String.valueOf(currentcours++));
			tab.getCellFormatter().addStyleName(currentRow, 0, "body");
			tab.setText(currentRow, 1, cours.getId());
			tab.getCellFormatter().addStyleName(currentRow, 1, "body");
			tab.setText(currentRow, 2, cours.getDescription());
			tab.getCellFormatter().addStyleName(currentRow, 2, "body");
			tab.setText(currentRow, 3, String.valueOf(cours.getNbCredit()));
			tab.getCellFormatter().addStyleName(currentRow, 3, "body");
		}
		currentRow = tab.getRowCount();
		tab.setHTML(currentRow, 0, "Total");
		tab.getCellFormatter().addStyleName(currentRow, 0, "body");
		tab.getFlexCellFormatter().setColSpan(currentRow, 0, 3);
		tab.setHTML(currentRow, 1, String.valueOf(0));
		tab.getCellFormatter().addStyleName(currentRow, 1, "body");

		currentRow = synt.getRowCount();
		this.refreshGrilleIndispo();
		synt.setWidget(currentRow, 0, indispoPanel);
		synt.setWidget(currentRow, 1, tab);
		
		currentRow = synt.getRowCount();
		synt.getRowFormatter().addStyleName(currentRow, "body");
		synt.setText(currentRow, 0, "Nombre de grille trouvée(s)");
		synt.setText(currentRow, 1, String.valueOf(planificateur.getGrilles().size()));

		synthesePanel.add(synt);
	}
	
	protected abstract class CellObjectTableListener implements TableListener {
		private int rowNum = 0;
		private int cellNum = 0;
		private Object obj = null;

		public CellObjectTableListener(Object obj, int row, int cell) {
			this.rowNum = row;
			this.cellNum = cell;
			this.obj = obj;
		}

		public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
//			setStatus("onCellClicked : '"+row+"'...");
			if((row == rowNum) && (cell == cellNum)) {
				doAction(obj);
			}
		}
		
		protected abstract void doAction(Object obj);
	}

	protected abstract class RowObjectTableListener implements TableListener {
		private int rowNum = 0;
		private Object obj = null;

		public RowObjectTableListener(Object obj, int row) {
			this.rowNum = row;
			this.obj = obj;
		}

		public void onCellClicked(SourcesTableEvents sender, int row, int cell) {
//			setStatus("onCellClicked : '"+row+"'...");
			if(row == rowNum) {
				doAction(obj);
			}
		}
		
		protected abstract void doAction(Object obj);
	}

	protected void refreshSelectionPanel() {
		selectionPanel.clear();
//		selectionPanel.setWidth("100%");

		FlexTable tab = new FlexTable();
		tab.setBorderWidth(1);
		tab.setWidth("100%");
		int currentRow = tab.getRowCount();
		tab.getRowFormatter().addStyleName(0, "tableheader");
		tab.getFlexCellFormatter().setColSpan(currentRow, 0, 2);
		tab.setText(currentRow, 0, "Cours");
		tab.getCellFormatter().addStyleName(currentRow, 0, "tableheader");
		tab.setHTML(currentRow, 1, "Crédit(s)");
		tab.getCellFormatter().addStyleName(currentRow, 1, "tableheader");
		tab.getColumnFormatter().setWidth(1, "400");
		tab.getColumnFormatter().setWidth(2, "10%");
		for(Iterator iter = listeCours.iterator(); iter.hasNext();) {
			Cours cours = (Cours) iter.next();
			currentRow = tab.getRowCount();
			String style = "coursachoisir";
			if(planificateur.getCoursChoisis().contains(cours)) {
				style = "courschoisi";
				tab.addTableListener(new RowObjectTableListener(cours, currentRow) {
					protected void doAction(Object cours) {
						removeCours((Cours) cours);
					}
				});
			} else if(!planificateur.getCoursSansConflits().contains(cours)) {
				style = "coursenconflits";
			} else {
				tab.addTableListener(new RowObjectTableListener(cours, currentRow) {
					protected void doAction(Object cours) {
						addCours((Cours) cours);
					}
				});
			}
			tab.getRowFormatter().addStyleName(currentRow, style);
			tab.getRowFormatter().setVerticalAlign(currentRow, VerticalPanel.ALIGN_TOP);
			tab.setText(currentRow, 0, cours.getId());
			tab.getCellFormatter().addStyleName(currentRow, 0, style);

			VerticalPanel descPanel = new VerticalPanel();
			Label descLabel = new Label(cours.getDescription());
			descLabel.setStyleName(style);
			descPanel.add(descLabel);

			FlexTable tabDesc = new FlexTable();
			tabDesc.setBorderWidth(1);
			for(Iterator gIter = cours.getListeGroupe().iterator(); gIter.hasNext();) {
				GroupeCours groupe = (GroupeCours) gIter.next();
				int currentRow2 = tabDesc.getRowCount();

				tabDesc.setText(currentRow2, 0, groupe.getId());
				tabDesc.getCellFormatter().addStyleName(currentRow2, 0, style);
				tabDesc.getRowFormatter().setVerticalAlign(currentRow2, VerticalPanel.ALIGN_TOP);
				if(!groupe.getListePeriodes().isEmpty()) {
					tabDesc.getFlexCellFormatter().setRowSpan(currentRow2, 0, groupe.getListePeriodes().size());
		
					int col = 1;
					for(Iterator pIter = groupe.getListePeriodes().iterator(); pIter.hasNext();) {
						PeriodeCours periode = (PeriodeCours) pIter.next();
		
						tabDesc.getCellFormatter().addStyleName(currentRow2, col, style);
						tabDesc.setText(currentRow2, col++, PeriodeCours.getJourName(periode.getJour()));
						tabDesc.getCellFormatter().addStyleName(currentRow2, col, style);
						tabDesc.setText(currentRow2, col++, periode.getHeureDebut()+" - "+periode.getHeureFin());
						tabDesc.getCellFormatter().addStyleName(currentRow2, col, style);
						tabDesc.setText(currentRow2, col, periode.getTypeName());
						col = 0;
						currentRow2 = tabDesc.getRowCount();
					}
				}
			}
			descPanel.add(tabDesc);

			tab.setWidget(currentRow, 1, descPanel);
			tab.getCellFormatter().addStyleName(currentRow, 1, style);

			tab.setText(currentRow, 2, String.valueOf(cours.getNbCredit()));
			tab.getCellFormatter().addStyleName(currentRow, 2, style);
		}

		selectionPanel.add(tab);
	}

	public void refreshGrilleIndispo() {
		indispoPanel.clear();
		indispoPanel.setWidth("100%");

		String plagesHoraires[][] = {
				{ "08:00", "13:00" },
				{ "13:30", "17:00" },
				{ "18:00", "23:00" }
		};
				
		FlexTable tab = new FlexTable();
		tab.setBorderWidth(1);
		tab.setWidth("100%");
		int currentRow = tab.getRowCount();
		for(int row = 0; row < plagesHoraires.length; row++) {
			currentRow = tab.getRowCount();

			for(int col = 0; col < 6; col++) {
				String startTime = plagesHoraires[row][0];
				String endTime = plagesHoraires[row][1];

				Collection evts = planificateur.getIndisponibilitesInside(col+2, startTime, endTime);
				if (evts.isEmpty()) {
					tab.getCellFormatter().addStyleName(currentRow, col, "dispo");
					tab.setHTML(currentRow, col, "&nbsp;");
					Indisponibilite indisp = new Indisponibilite();
					indisp.setJour(col+2);
					indisp.setHeureDebut(startTime);
					indisp.setHeureFin(endTime);
					tab.addTableListener(new CellObjectTableListener(indisp, currentRow, col) {
						public void doAction(Object indisp) {
							addIndispo((Indisponibilite) indisp);
						}
					});
				} else {
					EvenementIF evt = (EvenementIF) (evts.iterator().next());
					tab.getCellFormatter().addStyleName(currentRow, col, "indispo");
					tab.setHTML(currentRow, col, "&nbsp;");
					tab.addTableListener(new CellObjectTableListener(evt, currentRow, col) {
						public void doAction(Object indisp) {
							removeIndispo((Indisponibilite) indisp);
						}
					});
				}
			}
		}
		
		indispoPanel.add(tab);
	}

	public void refreshGrillePanel() {
		grillePanel.clear();
		grillePanel.setWidth("100%");
		((VerticalPanel) grillePanel).setSpacing(10);

		for(Iterator gIter = planificateur.getGrilles().iterator(); gIter.hasNext();) {
			grillePanel.add(this.getGrillePanel((GrilleHoraire) gIter.next()));
		}
	}

	public Panel getGrillePanel(GrilleHoraire grille) {
		Panel panel = new VerticalPanel();
		panel.setWidth("100%");

		String plagesHoraires1[][] = {
				{ "08:00", "13:00" },
				{ "13:30", "17:00" },
				{ "18:00", "23:00" }
		};
				
		String plagesHoraires2[][] = {
						{"08:00", "09:00"},
						{"09:00", "10:00"},
						{"10:00", "11:00"},
						{"11:00", "12:00"},
						{"12:00", "13:00"},
						{"13:00", "14:00"},
						{"14:00", "15:00"},
						{"15:00", "16:00"},
						{"16:00", "17:00"},
						{"17:00", "18:00"},
						{"18:00", "19:00"},
						{"19:00", "20:00"},
						{"20:00", "21:00"},
						{"21:00", "22:00"},
						{"22:00", "23:00"},
		               };

		String plagesHoraires[][] = plagesHoraires1;
		//Object test = request.getAttribute("grilleIdx");
		//if(test == null) {
		//plagesHoraires = plagesHoraires2;
		//}

		Object test = new Object();
		
		FlexTable tab = new FlexTable();
		tab.setBorderWidth(1);
		tab.setWidth("100%");
		int currentRow = tab.getRowCount();
		for(int row = 0; row < plagesHoraires.length+1; row++) {
			currentRow = tab.getRowCount();

			for(int col = 0; col < 7; col++) {
				if(row == 0) {
					String txt = "";
					switch(col) {
					case 0:
						tab.getRowFormatter().addStyleName(currentRow, "tableheader");
						txt = "";
						tab.getColumnFormatter().setWidth(col, "10%");
						break;
					default:
						txt = PeriodeCours.getJourName(col+1);
						tab.getColumnFormatter().setWidth(col, "15%");
						break;
					}
					tab.setText(currentRow, col, txt);
					tab.getCellFormatter().addStyleName(currentRow, col, "tableheader");
				} else {
					String startTime = plagesHoraires[row-1][0];
					String endTime = plagesHoraires[row-1][1];

					if(col == 0) {
						tab.setHTML(currentRow, col, startTime+"<br/>"+endTime);
						tab.getCellFormatter().addStyleName(currentRow, col, "tableheader");
					} else {
						Collection evts = grille.getEvenementsInside(col+1, startTime, endTime);
						if (evts.isEmpty()) {
							tab.getCellFormatter().addStyleName(currentRow, col, "dispo");
							tab.setHTML(currentRow, col, "&nbsp;");
						} else {
							EvenementIF evt = (EvenementIF) (evts.iterator().next());
							if ((row == 1) || (evt.getHeureDebut().compareTo(plagesHoraires[row - 2][1]) >= 0) || ((evt instanceof Indisponibilite) && (test == null))) {
								int rowspan = 1;
								for (int j = row; j < plagesHoraires.length; j++) {
									if (evt.getHeureFin().compareTo(plagesHoraires[j][0]) > 0) {
										rowspan++;
									} else {
										break;
									}
								}
								if (evt instanceof Indisponibilite) {
									if(test != null) {
										tab.getCellFormatter().addStyleName(currentRow, col, "indispo");
										if(rowspan > 1) tab.getFlexCellFormatter().setRowSpan(currentRow, col, rowspan);
										tab.setText(currentRow, col, "Bloque");
									} else {
										tab.getCellFormatter().addStyleName(currentRow, col, "dispo");
										tab.setHTML(currentRow, col, "&nbsp;");
									}
								} else if (evt instanceof PeriodeCours) {
									PeriodeCours currentPeriode = ((PeriodeCours) evt);
									GroupeCours currentGroupe = currentPeriode.getGroupe();
									Cours cours = currentGroupe.getCours();
									String local = currentPeriode.getLocal();
									if(local == null) {
										local = "";
									}
									tab.addTableListener(new CellObjectTableListener(cours, currentRow, col) {
										public void doAction(Object cours) {
											removeCours((Cours) cours);
										}
									});
									tab.setHTML(currentRow, col, ""+currentGroupe+"<br/>"+currentPeriode.getTypeName()+"<br/>"+evt.getHeureDebut()+" - "+evt.getHeureFin()+"<br/>"+local);
									if(rowspan > 1) tab.getFlexCellFormatter().setRowSpan(currentRow, col, rowspan);
									tab.getCellFormatter().addStyleName(currentRow, col, ((PeriodeCours) evt).getTypeName().toLowerCase());
								}
							}
						}
					}
				}
			}
		}

		panel.add(tab);
		return panel;
	}
}
