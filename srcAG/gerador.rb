#!/usr/bin/env ruby -wKU

def dist2(p1, p2)
  return (p1[0] - p2[0])**2 + (p1[1] - p2[1])**2
end

# vértices raio prob_terminal
n = ARGV[0] ?  ARGV[0].to_i : 100
r = ARGV[1] ?  ARGV[1].to_f : 0.2
prob_terminal = ARGV[2] || 0.1
pos = []
n.times do |v|
  pos[v] = [rand, rand]
end
edges = []
pos.each_with_index do |p1, i|
  pos.each_with_index do |p2, j|
    edges << [i, j] if i < j && dist2(p1,p2) <= r*r
  end
end

# id term x y z
puts "#{n} #{edges.size}\n"
pos.each_with_index do |p, v|
  terminal = rand <= prob_terminal || v == 0 ? 1 : 0
  puts "#{v+1} #{terminal} #{p[0]} #{p[1]} 0.0 "
end
puts
edges.each do |e|
  puts "#{e[0]+1}-#{e[1]+1} 1"
end
