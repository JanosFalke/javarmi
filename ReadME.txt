#Normalement pas besoin de la compilation MAIS au cas ou! : 
	- (S'il faut compiler les .java -> .class ==> javac Serveur/Serveur.java Diviseur/Diviseur.java Operations/Nombre.java Compute/Compute.java Compute/Task.java Compute/ComputeEngine.java)
	- (S'il faut compiler le .jar -> ==> jar -cvf standardExecution.jar Serveur/Serveur.class Diviseur/Diviseur.class Operations/Nombre.class Compute/Compute.class Compute/Task.class Compute/ComputeEngine.class)




********************************Lancement normal********************************
#Lancement dans des terminaux differents (un terminal par serveur):
Lancer d'abord le/s serveur/s(ComputeEngine/s), puis le diviseur (client)!




_On peut passer des arguments en commande également: 
#Pour le serveur, 
	-p --> port du serveur (par défaut 1099)
	-a --> adresse du serveur (par défaut l'adresse locale)
	-v --> détails des traitements / verbose (par defaut true) dans les serveurs

#Pour le diviseur, 
	-l --> login (par defaut 'postgres')
	-m --> mot de passe (par defaut 'pgAdmin')
	-n --> nom de la bd (par defaut 'TEAArchi') -> bd va etre crée dans le diviseur
	-t --> NbThreads/Diviseurs (par defaut 1) ==> ne pas besoin si adresses sont precisés
						  ==> si pas d'adresse données (-a) -> l'adresse par défaut choisi -> adresse locale et port RMI 1099 pour 1 (pour 2 adresse local et port RMI 1099 et 1100)
	-b --> debut des entiers a traiter (par defaut 1)
	-e --> fin des entiers a traiter (par defaut 100)
	-v --> détails des traitements / verbose (par defaut true) dans le diviseur
	-a --> adresses+port des serveurs sur lesquels ont va repartir les calculs 
		==> une seule adresse --> x.x.x.x:xxxx
		==> plusieurs adresses --> {x.x.x.x:xxxx;y.y.y.y:yyyy} 
		==> séparation des adresses avec ; !!!




#Serveur / ComputeEngine :
Pour avoir l'accès aux ports (a cause du RMISecurityManager) --> -Djava.security.policy=java.policy
 
- Commande pour le serveur:
	> java -Djava.security.policy=java.policy Serveur/Serveur -a [adresse] -p [port] -v [verbose]

	Exemple avec args: java -Djava.security.policy=java.policy Serveur/Serveur -a 127.0.0.1 -p 1100 -v false




#Diviseur (Client):
Pour avoir l'accès aux ports (a cause du RMISecurityManager) --> -Djava.security.policy=java.policy
Pour donner un driver pour la connexion à la base de données --> -cp lib/postgresql-42.2.5.jar

- Commande pour le diviseur:
	SOUS WINDOWS:
	> java -Djava.security.policy=java.policy -cp lib/postgresql-42.2.5.jar; Diviseur/Diviseur -l [login] -m [motDePasse] -n [nomBD]  -t [NbDiviseurs] -b [Debut] -e [Fin] -v [verbose] -a [{adresses:ports}]

		Exemple avec args: java -Djava.security.policy=java.policy -cp lib/postgresql-42.2.5.jar; Diviseur/Diviseur -l postgres -m pgAdmin -n TEAArchi -b 0 -e 50 -v false -a {127.0.0.1:1099;127.0.0.1:1100}
		(Ici: postgres (login), pgAdmin (mot de passe), TEAArchi (nom de bd), 0 (debut), 50 (fin), false (verbose), adresses et ports


	SOUS LINUX/MAC:
	> java -Djava.security.policy=java.policy -cp lib/postgresql-42.2.5.jar: Diviseur/Diviseur -l [login] -m [motDePasse] -n [nomBD]  -t [NbDiviseurs] -b [Debut] -e [Fin] -v [verbose] -a [{adresses:ports}]


	(; devient : sous Linux dans la commande ... jar; ... -> ... jar: ...)







********************************Lancement avec l'executable .jar********************************


#Ici on a la meme possiblité d'ajouter les parametres/options comme precedemment.
#Meme usage donc pour l'utilisations voir explications plus haut

___Lancer serveur et diviseur dans des terminaux differents! (un terminal par serveur)___
Lancer d'abord le serveur/computeEngine, puis le diviseur (client)!


#Serveur / ComputeEngine :
- Commande pour le serveur:
	> java -Djava.security.policy=java.policy -cp ./standardExecution.jar Serveur/Serveur -a [adresse] -p [port] -v [verbose]
	(Ici on est obligé de passer la java.policy sinon le RMISecurityManager refuse la connexion)

#Diviseur (Client):
- Commande pour le diviseur:
	SOUS WINDOWS:
	> java -Djava.security.policy=java.policy -cp lib/postgresql-42.2.5.jar;standardExecution.jar Diviseur/Diviseur -l [login] -m [motDePasse] -n [nomBD]  -t [NbDiviseurs] -b [Debut] -e [Fin] -v [verbose] -a [{adresses:ports}]


	SOUS LINUX/MAC:
	> java -Djava.security.policy=java.policy -cp lib/postgresql-42.2.5.jar:standardExecution.jar Diviseur/Diviseur -l [login] -m [motDePasse] -n [nomBD]  -t [NbDiviseurs] -b [Debut] -e [Fin] -v [verbose] -a [{adresses:ports}]
	

	(; devient : sous Linux dans la commande ... jar;standa ... -> ... jar:standa ...)
	(Ici on est obligé de passer aussi le driver (.jar) sinon on ne peut pas acceder a la base de donnees!)







