############################################################
# Description: Pacote de funcoes estatisticas              #
# Author:      Eduardo Freire Nakamura                     #
# Date:        06/04/2004                                  #
############################################################

package Normal;

# retorna a média de uma amostra
# parâmetros:
#  - amostra: vetor com os valores da amostra
sub mediaAmostral
{
    # variáveis locais
    my(@amostra, $somatorio, $n, $i);
    
    # obtém os parâmetros
    @amostra = @_; # amostra
    
    $n = $#amostra + 1;
    $somatorio = 0;
    for ($i = 0; $i < $n; $i++) {
        $somatorio += $amostra[$i];
    }
    return $somatorio / $n;
}

# retorna o desvio padrão da amostra
# parâmetros:
#  - amostra: vetor com os valores da amostra
sub desvioPadrao 
{
    # variáveis locais
    my(@amostra, $soma1, $soma2, $n, $i);

    # obtém os parâmetros
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

# retorna a margem de erro para o intervalo de confiança
# parâmetros:
#  - gr:    grau de confiança (90%, 95%, 99%)
#  - sigma: desvio padrão
#  - n:     tamanho da amostra
sub margemDeErro 
{
    # variáveis locais
    my($gr, $desvio, $n, $z);

    # obtém os parâmetros
    ($gr, $desvio, $n) = @_;
    
    # valores crítico para graus de confiança de 90%, 95% e 99%
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
