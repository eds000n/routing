############################################################
# Description: Pacote para analise de traces do NS         #
# Author:      Eduardo Freire Nakamura                     #
# Date:        29/08/2005                                  #
############################################################

package NSInfra;

use Normal;
use List::Util qw[min max];
# variaveis do pacote
my $detection, $chfull, $chdepth, $fsink, $fch, $sdata, $aggpkts, $recvpkts, $deg, $tree, $skdist;

# Limpa as variaveis usadas na analise do trace
# parametros:
#  - nenhum
sub zeraAnalise
{
	undef($detection);
	undef($chfull);
	undef($chdepth);
	undef($fsink);
	undef($fch);
	undef($sdata);
	undef($firstsentpkt);
	undef($lastsentpkt);
	undef($firstrecvpkt);
	undef($lastrecvpkt);
	undef($aggpkts);
	undef($recvpkts);
	
	undef($tree);	 	undef($skdist)
}

# Analisa o trace para obter os dados de consumo,
# perdas, nro. de nos e atraso medio dos pacotes
# parametros:
#  - trace: arquivo de trace
sub analisarTrace
{
	zeraAnalise();
	
	# variáveis locais
	my(@elementos);
	my($trace);

	# obtém os parâmetros
	($trace) = @_; # arquivo de trace
    
	# abre o arquivo de trace
	open( TRACE, $trace ) || die "NS::analisarTrace : Erro ao abrir arquivo $trace\n";
    
	$detection    = 0;
	$chfull       = 0;
	$chdepth      = 0;
	$fsink        = 0;
	$fch          = 0;
	$sdata        = 0;
	$aggpkts      = 0;
	$recvpkts     = 0;
	
	$tree		  = 0;		$skdist		  = 1000;

	# Le cada linha do arquivo
	while ( $linha = <TRACE> ) {							if ( $linha =~ /^SkDist.*/ ) 		{			@elementos = split( " ", $linha );			if ($skdist > $elementos[3]){				$skdist = $elementos[3];			}					}
		# conta o numero de nos que detectaram eventos
		if ( $linha =~ /^Detection.*/ ) 
		{			
			$detection = $detection + 1;
		}
		
		# conta o numero de cluster-heads full
		if ( $linha =~ /^ChFull.*/ ) 
		{
			$chfull = $chfull + 1;
		}
		
		# conta o numero de cluster-heads depth
		if ( $linha =~ /^ChDepth.*/ ) 
		{
			$chdepth = $chdepth + 1;
		}
		
		# conta o numero floodings sink
		if ( ($linha =~ /^Flooding.*wsn-infra-sink.*/) || ($linha =~ /^Sink.*/)) 
		{
			$fsink = $fsink + 1;
		}
		
		# conta o numero floodings CHs
		if ( $linha =~ /^Flooding.*wsn-infra-ch\b.*/ ) 
		{
			$fch = $fch + 1;
		}
		
		# conta o numero pacotes de dados
		if ( $linha =~ /^(\s)?Sdata.*/ ) 
		{
			$sdata = $sdata + 1;
		}
		
		# verifica pacotes recebidos no sink
		if ( $linha =~ /^Rdata.*Nd 1\s.*/ ) 
		{
			$recvpkts = $recvpkts + 1;
			
			# Quebra a linha em um array
			@elementos = split( " ", $linha );
			# Analisa cada parametro do array
			for( $i = 0; $i <= $#elementos; $i++) 
			{
				# Armazena o valor do tempo
				if($elementos[$i] =~ /Ap\b/) 
				{ 
					$aggpkts = $aggpkts + $elementos[$i + 1]; 
				}
			}
		}
	
		# verifica grau medio dos nos
		if ( $linha =~ /^Degree.*/ ) 
		{
			# Quebra a linha em um array
			@elementos = split( " ", $linha );			$deg = $elementos[3];

		}
		# verifica peso total  da arvore
		if ( $linha =~ /^Tree.*/ ) 
		{			
			# Quebra a linha em um array
			@elementos = split( " ", $linha );
			# Analisa cada parametro do array
			for( $i = 0; $i <= $#elementos; $i++) 
			{
				# Armazena o valor do tempo
				if($elementos[$i] =~ /.*dist\b/) 
				{ 				
					$tree = $tree + $elementos[$i + 1]; 
				}
			}
		}
	}	#print "SKDIST -> $skdist \n";
}

# 
sub obterDeteccoes
{
	if(!defined($detection)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterDeteccoes()!!\n"; } 
	return $detection;
}

# 
sub obterPacotesChFull
{
	if(!defined($chfull)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterPacotesChFull()!!\n"; } 
	return $chfull;
}

# 
sub obterPacotesChDepth
{
	if(!defined($chdepth)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterPacotesChDepth()!!\n"; } 
	return $chdepth;
}
	

# 
sub obterSinkOverhead
{
	if(!defined($fsink)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterSinkOverhead()!!\n"; } 
	return $fsink;
}
	
# 
sub obterChOverhead
{
	if(!defined($fch)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterChOverhead()!!\n"; } 
	return $fch;
}
	
# 
sub obterPacotesTransmitidos
{
	if(!defined($sdata)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterPacotesTransmitidos()!!\n"; } 
	return $sdata;
}
	
# 
sub obterPacotesRecebidos
{
	if(!defined($recvpkts)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterPacotesRecebidos()!!\n"; } 
	return $recvpkts;
}
	
# 
sub obterPacotesAgregados
{
	if(!defined($aggpkts)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterPacotesAgregados()!!\n"; } 	#print"AGG - $aggpkts\n"; 
	#$aggpkts=1;
	return $aggpkts;
}
	
# 
sub obterTaxaDeAgregacao
{
	if(!defined($aggpkts) || !defined($recvpkts)) 
	{ 
		die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterTaxaDeAgregacao()!!\n"; 
	} 
	return $recvpkts / $aggpkts;
}

# 
sub obterGrauMedio
{
	if(!defined($deg)) 
	{ 
		die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterGrauMedio()!!\n"; 
	} 
	return $deg;
}sub obterSkDist{	if(!defined($skdist)) 	{ 		die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterSkDist()!!\n"; 	} 	return $skdist;}
sub obterTree
{
	if(!defined($tree)) { die "Execute NS::analisarTrace(\$TRACE) antes de NSInfra::obterDeteccoes()!!\n"; } 
	return $tree;
}
@EXPORT = qw(
analisarTrace
obterDeteccoes
obterPacotesChFull
obterPacotesChDepth
obterSinkOverhead
obterChOverhead
obterPacotesTransmitidos
obterPacotesRecebidos
obterPacotesAgregados
obterTaxaDeAgregacao
obterGrauMedioobterSkDist
)
