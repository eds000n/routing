#!/usr/bin/perl 

use NSInfra;
use Normal;
#use Statistics::Descriptive;

####Configuracao da simulacao

#my $simulacao = "numevent"; ## time, esize, numevent, ndensity, rrange,  size

my @simulacoes = ("numevent","time","esize","ndensity","rrange","size");

my $simula = 1;  #simula ou apenas analisa os traces?

##Parametros comuns a todas simulacoes

my @projetos = ("InfraLeo","Infra");

my $pi = 3.141592;
my $pidensity = 8.48;
my $rounds = 99;
my $outdir      = "logs"; # diretorio de traces
my @deteccoes, @chfull, @chdepth, @overhead, @datapkts, @taxaagg, @sumario, @deg, @skdist, @rcvdata, @nodedensity, @snodedensity, @routingefficiency;
my $datarate = 60;
my $xy = 700;
my $sumfile     = "sumario.plt" ; # nome do arquivo de sumario
my $cdensity;
my $evstart     = 1000            ; # event start time
my $evstop      = 14400         ; # event stop time
my $simtime     = 15400          ; # simulation time
my $seed = 0; #semente aleatoria

#decide o tipo de interface da simulação (batch ou gui)
my $gui = 0;
my $guiarg = "batch";
my $javaCmd = "java";

if ($gui){
	$javaCmd   = "java";
	$guiarg = "gui";
}else {
	$javaCmd   = "java -Djava.awt.headless=true ";
	$guiarg = "batch";
}
### Parametros especificos de cada simulacao
foreach $simulacao (@simulacoes){
	my @numevents;
	if ($simulacao eq "numevent"){
		@numevents = (2,3,4,5,6);
	}elsif($simulacao eq "time"){
		@numevents = (3);
	}else{
		@numevents = (1);
	}
	
	my @sizelist;
	if ($simulacao eq "size"){ 
		@sizelist = (121,256,529,1024);
	}elsif ($simulacao eq "ndensity"){
		@sizelist = (529,1024,1553,2048,4096);	
		my $ndensity;
	}else {
		@sizelist = (529);
	}
	
	my @srangelist;
	if ($simulacao eq "esize"){
		@srangelist = (50,60,70,80,90,100);
	} else {
		@srangelist = (80);
	}
	
	my @rrangelist;
	if ($simulacao eq "rrange"){ 
		@rrangelist = (50,60,70,80,90,100);
	}else {
		@rrangelist = (50);
	}
	
	my @times;
	if ($simulacao eq "time"){
		@times = (1200, 3600,7200,10800,14400);
	}else {
		@times = (10800);
	}
	
	print("\nIniciando Analise de simulacoes...\n\n"); 
	
	
	
	foreach $projeto (@projetos){
		$outdir      = "logs/$projeto/traces-$simulacao";
		unless (-e "$outdir") { system("mkdir -p $outdir"); }
		system("rm -f $outdir/$sumfile; touch $outdir/$sumfile");
		# insere cabecalho no arquivo de sumario
		gravarCabecalho("$outdir/$sumfile");
		
		foreach $numevent (@numevents){
			foreach $time (@times){
				$evstop = $time + $evstart; #calcula a duração do(s) evento(s) (eles iniciam em t=1000)
				$simtime = $evstop + 1000; #final da simulação (para dar tempo para os últimos pacotes enviados chegarem)
				
				foreach $esize (@srangelist){
					foreach $rrange (@rrangelist){
						foreach $size (@sizelist){
							$ndensity = $size;
							# apaga dados extraídos
								undef @deteccoes;
								undef @chfull;
								undef @chdepth;
								undef @overhead;
								undef @datapkts;
								undef @agregados;
								undef @taxaagg;
								undef @sumario;
								undef @deg;
								undef @skdist;
								undef @rcvdata;
								undef @nodedensity;
								undef @snodedensity;
								undef @routingefficiency;
								
								#calcula as dimensões caso a simulação seja a size
								if ($simulacao eq "size"){
									$xy =  int(sqrt(($rrange * $rrange * $size * $pi) / $pidensity));
								}
								#calcula a posição do(s) evento(s)
								@eventposition3 = ($xy - $esize,$xy-$esize);
								@eventposition1 = ($xy - $esize, $esize);
								@eventposition2 = ($esize, $xy - $esize);
								@eventposition4 = ($xy-$esize,$xy/2);
								@eventposition5 = ($xy/2,$xy-$esize);
								@eventposition6 = ($esize, $xy/2);
								
								
								print("\n");
								print("tamanho (nos)            : $size\n");
								print("raio de comunicacao (m)  : $rrange\n");
								print("dimensoes (m^2)          : $xy x $xy\n");
								print("raio de sensoriamento (m): $srange\n");			
				        		
									
							for ($scenario = 1; $scenario <= $rounds; $scenario++){
								$mytrace = "$outdir/$$simulacao/tr-$scenario.nak";
								print "$mytrace\n"; 
								
								$seed = $scenario*529*7765476; #uma semente para cada cenário 
								
								#tem que reconfigurar o arquivo de log pois para o sinalgo
								#o diretorio "log" já é padrão							
								unless (-e "$outdir/$$simulacao") { system("mkdir -p $outdir/$$simulacao"); }
								$outdir      = "$projeto/traces-$simulacao";
								$mytrace = "$outdir/$$simulacao/tr-$scenario.nak";
									
									
								#preparação para rodar a simulação	
								$cmd = "$javaCmd  -cp binaries/bin sinalgo.Run ".
										"-project $projeto " . 
										"-gen $size  $projeto:$projeto"."Node Infra:InfraDistributionModel NoMobility " .
										"-$guiarg ".		
										"AutoStart=true " . 
										"-overwrite dimX=$xy ".
										"-overwrite dimY=$xy ".
										"-overwrite Event/EventSize=". $esize*2 ." ".
										"-overwrite Event/Xposition1=$eventposition1[0] ".
										"-overwrite Event/Yposition1=$eventposition1[1] ".
										"-overwrite Event/Xposition2=$eventposition2[0] ".
										"-overwrite Event/Yposition2=$eventposition2[1] ".
										"-overwrite Event/Xposition3=$eventposition3[0] ".
										"-overwrite Event/Yposition3=$eventposition3[1] ".
										"-overwrite Event/Xposition4=$eventposition4[0] ".
										"-overwrite Event/Yposition4=$eventposition4[1] ".
										"-overwrite Event/Xposition5=$eventposition5[0] ".
										"-overwrite Event/Yposition5=$eventposition5[1] ".
										"-overwrite Event/Xposition6=$eventposition6[0] ".
										"-overwrite Event/Yposition6=$eventposition6[1] ".
										"-overwrite GeometricNodeCollection/rMax=$rrange ".
										"-overwrite UDG/rMax=$rrange ".
										"-overwrite Event/DataRate=$datarate ".
										"-overwrite Event/EventStart1=$evstart ".
										"-overwrite Event/EventStart2=$evstart ".
										"-overwrite Event/EventStart3=$evstart ".
										"-overwrite Event/EventStart4=$evstart ".
										"-overwrite Event/EventStart5=$evstart ".
										"-overwrite Event/EventStart6=$evstart ".
										"-overwrite Event/EventEnd=$evstop ".
										"-overwrite SimTime=$simtime ".
										"-overwrite Log/LogFile=$mytrace ".
										"-overwrite Event/NumEvents=$numevent ".
										"-overwrite useFixedSeed=true ".		
										"-overwrite fixedSeed=$seed ".
										"";
								# imprime a linha de comando para ver se está tudo ok
								print"$cmd";
								
								#execução da simulação
								if ($simula){
							    	system($cmd);
								}
								
								# retorna o diretório "log" para poder fazer a análise das simulações
							    $outdir      = "logs/$projeto/traces-$simulacao";
								$mytrace = "$outdir/$$simulacao/tr-$scenario.nak";								
								
								#se for apenas análise de logs, descompactar o arquivo que foi gerado na última
								#simulação
								if(!$simula){
									system("gunzip $mytrace");
								}
									
								# analisa o trace
								
								$cdensity = ($pi * $rrange**2) / ($xy**2);
								
								NSInfra::analisarTrace($mytrace);
								
								if(NSInfra::obterPacotesAgregados() == 0 || (($projeto eq "Infra")&& NSInfra::obterSkDist() ==1000) )
								{
								    print "Erro em  cenario $scenario com $numevent evento(s) e Raio de comm $rrange com $size nó(s)\n";
								 	break;
								}
								else
								{
								    push(@deteccoes, NSInfra::obterDeteccoes());
								    push(@chfull,    NSInfra::obterPacotesChFull());
								    push(@chdepth,   NSInfra::obterPacotesChDepth());
								    push(@overhead,  1.0 + NSInfra::obterSinkOverhead() + NSInfra::obterChOverhead());
								    push(@datapkts,  NSInfra::obterPacotesTransmitidos());
								    push(@taxaagg,   1.0 - NSInfra::obterTaxaDeAgregacao());
								    push(@deg, NSInfra::obterGrauMedio());
								    if ($projeto eq "Infra"){
								    	push(@skdist, NSInfra::obterSkDist());
								    	push(@rcvdata, NSInfra::obterPacotesRecebidos());
								    }
								    push(@nodedensity, $size*$cdensity);
								    push(@snodedensity, NSInfra::obterDeteccoes()*$cdensity);
								    push(@routingefficiency, (1.0 + NSInfra::obterSinkOverhead() 
								    						+ NSInfra::obterChOverhead()
								    						+ NSInfra::obterPacotesChFull()
								    						+ NSInfra::obterDeteccoes()
								    						+ NSInfra::obterPacotesTransmitidos()
								    						)/
								    						(NSInfra::obterDeteccoes()* ($time/$datarate))
					 			    						);
								}
							   system("rm -f $mytrace.gz");
							   system("gzip $mytrace");
							}##foreach $scenario
		
							# armazena dados do sumario
							push(@sumario,$time);
							push(@sumario,$numevent);
							push(@sumario,$size);
							push(@sumario,$esize);
							push(@sumario,$rrange);
							
							
							push(@sumario,Normal::mediaAmostral(@deteccoes));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@deteccoes), $rounds));
						
							push(@sumario,Normal::mediaAmostral(@chfull));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@chfull), $rounds));
							
							push(@sumario,Normal::mediaAmostral(@overhead));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@overhead), $rounds));
							
							push(@sumario,Normal::mediaAmostral(@datapkts));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@datapkts), $rounds));
							
							push(@sumario,Normal::mediaAmostral(@taxaagg));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@taxaagg), $rounds));
							
							push(@sumario,Normal::mediaAmostral(@deg));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@deg), $rounds));
							
							if ($projeto eq "Infra"){
								push(@sumario,Normal::mediaAmostral(@skdist));
								push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@skdist), $rounds));
								
								push(@sumario,Normal::mediaAmostral(@rcvdata));
								push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@rcvdata), $rounds));
							}
							
							push(@sumario,Normal::mediaAmostral(@nodedensity));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@nodedensity), $rounds));
							
							push(@sumario,Normal::mediaAmostral(@snodedensity));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@snodedensity), $rounds));
								
							push(@sumario,Normal::mediaAmostral(@routingefficiency));
							push(@sumario,Normal::margemDeErro(95, Normal::desvioPadrao(@routingefficiency), $rounds));
								
							# grava dados no arquivo do sumario
							gravarSumario("$outdir/$sumfile", @sumario);
						}##foreach $size
					}# foreach rrange
				}#foreach esize
			}#foreach @time
		}##foreach $numevents
	}##foreach $projetos
}#foreach $simulacao
sub gravarCabecalho
{
	# le os parametros
	my ($sumario) = @_;

	# Abre arquivo sumario
	open(SUMARIO, ">>$sumario") || die "Erro ao abrir arquivo $sumario\n";
	if ($projeto eq "Infra"){
		print SUMARIO "TIME\t",
			"NE\t", 
			"NN\t",
			"ES\t",
			"RRANGE\t",
			"DETECT\tE_DET\t",
			"CHFUL\tE_CHFUL\t",
			"CHOVER\tE_CHOVER\t",
			"SNTPKT\tE_SNT\t",
			"AGGRAT\tE_AR\t",
			"DEGREE\tE_DG\t",
			"SKDIST\tE_SKDIST\t",
			"RCVDATA\tE_RCVDATA\t",
			"NDENSITY\tE_NDENSITY\t",
			"SNDENSITY\tE_SNDENSITY\t",
			"REFFICIENCY\tE_REFFICIENCY\n";
	}else {
			print SUMARIO "TIME\t",
			"NE\t", 
			"NN\t",
			"ES\t",
			"RRANGE\t",
			"DETECT\tE_DET\t",
			"CHFUL\tE_CHFUL\t",
			"CHOVER\tE_CHOVER\t",
			"SNTPKT\tE_SNT\t",
			"AGGRAT\tE_AR\t",
			"DEGREE\tE_DG\t",
			"NDENSITY\tE_NDENSITY\t",
			"SNDENSITY\tE_SNDENSITY\t",
			"REFFICIENCY\tE_REFFICIENCY\n";
	}
	close(SUMARIO);
}


sub gravarSumario
{
	# le os parametros
	my ($sumario, @dados) = @_;
	my ($linha);

	# Abre arquivo sumario
	open(SUMARIO, ">>$sumario") || die "Erro ao abrir arquivo $sumario\n";

	if(defined(@dados))
	{
		$linha = join("\t",@dados);
		print SUMARIO "$linha\n";
	}
	close(SUMARIO);
}

sub exec_simula{
	$projeto = $_[0];
	
}
