# Makefile

FILE=paper
BIBFILE=paperbib

default:
	make clean
	make $(FILE).pdf

$(FILE).pdf: $(FILE).tex $(BIBFILE).bib
	pdflatex $(FILE)
	bibtex $(FILE)
	pdflatex $(FILE)
	pdflatex $(FILE)

$(FILE).ps: $(FILE).dvi
	dvips $(FILE)

$(FILE).dvi: $(FILE).tex $(BIBFILE).bib
	latex $(FILE)
	bibtex $(FILE)
	latex $(FILE)
	latex $(FILE)

update:
	svn update
	svn log|less

commit:
	svn commit
	make update

view:
	make $(FILE).pdf
	acroread $(FILE).pdf &

edit:
	gnuclient $(FILE).tex $(BIBFILE).bib &

clean:
	rm -f *~ *.dvi $(FILE).ps $(FILE).pdf *.log *.blg

full-clean:
	make clean
	rm -f *.aux *.bbl




# eof

