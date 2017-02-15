############################################################
# Description: Pacote de funcoes estatisticas              #
# Author:      Eduardo Freire Nakamura                     #
# Date:        06/04/2004                                  #
############################################################

package Normal;

# retorna a m�dia de uma amostra
# par�metros:
#  - amostra: vetor com os valores da amostra
sub mediaAmostral
{
    # vari�veis locais
    my(@amostra, $somatorio, $n, $i);
    
    # obt�m os par�metros
    @amostra = @_; # amostra
    
    $n = $#amostra + 1;
    $somatorio = 0;
    for ($i = 0; $i < $n; $i++) {
        $somatorio += $amostra[$i];
    }
    return $somatorio / $n;
}

# retorna o desvio padr�o da amostra
# par�metros:
#  - amostra: vetor com os valores da amostra
sub desvioPadrao 
{
    # vari�veis locais
    my(@amostra, $soma1, $soma2, $n, $i);

    # obt�m os par�metros
    @amostra = @_; # amostra
    
    $n = $#amostra + 1;
    $soma1 = 0;
    $soma2 = 0;
    for ($i = 0; $i < $n; $i++) {
        $soma1 += $amostra[$i] * $amostra[$i];
        $soma2 += $amostra[$i];
    }
    $soma2 *= $soma2;
    
    return sqrt( abs(($n*$soma1 - $soma2) / ($n * ($n - 1))) );    
}

# retorna a margem de erro para o intervalo de confian�a
# par�metros:
#  - gr:    grau de confian�a (90%, 95%, 99%)
#  - sigma: desvio padr�o
#  - n:     tamanho da amostra
sub margemDeErro 
{
    # vari�veis locais
    my($gr, $desvio, $n, $z);

    # obt�m os par�metros
    ($gr, $desvio, $n) = @_;
    
    # valores cr�tico para graus de confian�a de 90%, 95% e 99%
    $z{90} = 1.645;
    $z{95} = 1.960;
    $z{99} = 2.575;

    return $z{$gr} * ($desvio / sqrt($n));
}

@EXPORT = qw(desvioPadrao);
@EXPORT = qw(mediaAmostral);
@EXPORT = qw(margemDeErro);

# @a = (6.5, 6.6, 6.7, 6.8, 7.1, 7.3, 7.4, 7.7, 7.7, 7.7);
# print "Desvio Padrao: ", desvioPadrao(@a),"\n";
# print "Media        : ", mediaAmostral(@a),"\n";
# print "Erro         : ", margemDeErro(95, desvioPadrao(@a), $#a + 1),"\n";
# print "Erro         : ", margemDeErro(95, 0.62, 106),"\n";
