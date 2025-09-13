package com.github.azeroth.game.spell;


public class SpellImplicitTargetInfo {
    private static final StaticData[] DATA = new StaticData[]
            {
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Src, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.GobjItem, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontLeft),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.last, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Party, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackLeft),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontLeft),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Back),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Right),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Left),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.raid, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.raid, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.raid, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.RaidClass, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Back),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Right),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Left),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackLeft),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontLeft),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.channel, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.channel, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Back),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Right),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Left),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackRight),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.BackLeft),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.FrontLeft),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Traj, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Corpse, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.passenger, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.channel, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Nearby, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.Gobj, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.Src, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.UnitAndDest, SpellTargetReferenceTypes.last, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.raid, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Corpse, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.raid, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Summoned, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.Corpse, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.threat, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Tap, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.entry, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.target, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Line, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Line, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Line, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Cone, SpellTargetCheckTypes.Ally, SpellTargetDirectionTypes.Front),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.dest, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.dest, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.random),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.Default, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.unit, SpellTargetReferenceTypes.caster, SpellTargetSelectionCategories.area, SpellTargetCheckTypes.Enemy, SpellTargetDirectionTypes.NONE),
                    new StaticData(SpellTargetObjectTypes.NONE, SpellTargetReferenceTypes.NONE, SpellTargetSelectionCategories.Nyi, SpellTargetCheckTypes.Default, SpellTargetDirectionTypes.NONE)
            };

    private final Targets target;

    public spellImplicitTargetInfo() {
        this(0);
    }

    public spellImplicitTargetInfo(Targets target) {
        target = target;
    }

    public final boolean isArea() {
        return getSelectionCategory() == SpellTargetSelectionCategories.area || getSelectionCategory() == SpellTargetSelectionCategories.Cone;
    }

    public final SpellTargetSelectionCategories getSelectionCategory() {
        return _data[_target.getValue()].selectionCategory;
    }

    public final SpellTargetReferenceTypes getReferenceType() {
        return _data[_target.getValue()].referenceType;
    }

    public final SpellTargetObjectTypes getObjectType() {
        return _data[_target.getValue()].objectType;
    }

    public final SpellTargetCheckTypes getCheckType() {
        return _data[_target.getValue()].selectionCheckType;
    }

    private SpellTargetDirectionTypes getDirectionType() {
        return _data[_target.getValue()].directionType;
    }

    public final Targets getTarget() {
        return target;
    }

    public final float calcDirectionAngle() {
        var pi = MathUtil.PI;

        switch (getDirectionType()) {
            case Front:
                return 0.0f;
            case Back:
                return pi;
            case Right:
                return -pi / 2;
            case Left:
                return pi / 2;
            case FrontRight:
                return -pi / 4;
            case BackRight:
                return -3 * pi / 4;
            case BackLeft:
                return 3 * pi / 4;
            case FrontLeft:
                return pi / 4;
            case Random:
                return (float) RandomUtil.randomFloat() * (2 * pi);
            default:
                return 0.0f;
        }
    }

    public final SpellCastTargetFlags getExplicitTargetMask(tangible.RefObject<Boolean> srcSet, tangible.RefObject<Boolean> dstSet) {
        SpellCastTargetFlags targetMask = SpellCastTargetFlags.forValue(0);

        if (getTarget() == targets.DestTraj) {
            if (!srcSet.refArgValue) {
                targetMask = SpellCastTargetFlags.sourceLocation;
            }

            if (!dstSet.refArgValue) {
                targetMask = SpellCastTargetFlags.forValue(targetMask.getValue() | SpellCastTargetFlags.DestLocation.getValue());
            }
        } else {
            switch (getReferenceType()) {
                case Src:
                    if (srcSet.refArgValue) {
                        break;
                    }

                    targetMask = SpellCastTargetFlags.sourceLocation;

                    break;
                case Dest:
                    if (dstSet.refArgValue) {
                        break;
                    }

                    targetMask = SpellCastTargetFlags.DestLocation;

                    break;
                case Target:
                    switch (getObjectType()) {
                        case Gobj:
                            targetMask = SpellCastTargetFlags.GAMEOBJECT;

                            break;
                        case GobjItem:
                            targetMask = SpellCastTargetFlags.GameobjectItem;

                            break;
                        case UnitAndDest:
                        case Unit:
                        case Dest:
                            switch (getCheckType()) {
                                case Enemy:
                                    targetMask = SpellCastTargetFlags.UnitEnemy;

                                    break;
                                case Ally:
                                    targetMask = SpellCastTargetFlags.UnitAlly;

                                    break;
                                case Party:
                                    targetMask = SpellCastTargetFlags.UnitParty;

                                    break;
                                case Raid:
                                    targetMask = SpellCastTargetFlags.UnitRaid;

                                    break;
                                case Passenger:
                                    targetMask = SpellCastTargetFlags.UnitPassenger;

                                    break;
                                case RaidClass:
                                default:
                                    targetMask = SpellCastTargetFlags.unit;

                                    break;
                            }

                            break;
                    }

                    break;
            }
        }

        switch (getObjectType()) {
            case Src:
                srcSet.refArgValue = true;

                break;
            case Dest:
            case UnitAndDest:
                dstSet.refArgValue = true;

                break;
        }

        return targetMask;
    }

    public final static class StaticData {
        public SpellTargetobjectTypes objectType = SpellTargetObjectTypes.values()[0]; // type of object returned by target type
        public SpellTargetreferenceTypes referenceType = SpellTargetReferenceTypes.values()[0]; // defines which object is used as a reference when selecting target
        public SpellTargetSelectionCategories selectionCategory = SpellTargetSelectionCategories.values()[0];
        public SpellTargetCheckTypes selectionCheckType = SpellTargetCheckTypes.values()[0]; // defines selection criteria
        public SpellTargetdirectionTypes directionType = SpellTargetDirectionTypes.values()[0]; // direction for cone and dest targets
        public StaticData() {
        }
        public StaticData(SpellTargetObjectTypes obj, SpellTargetReferenceTypes reference, SpellTargetSelectionCategories selection, SpellTargetCheckTypes selectionCheck, SpellTargetDirectionTypes direction) {
            objectType = obj;
            referenceType = reference;
            selectionCategory = selection;
            selectionCheckType = selectionCheck;
            directionType = direction;
        }

        public StaticData clone() {
            StaticData varCopy = new StaticData();

            varCopy.objectType = this.objectType;
            varCopy.referenceType = this.referenceType;
            varCopy.selectionCategory = this.selectionCategory;
            varCopy.selectionCheckType = this.selectionCheckType;
            varCopy.directionType = this.directionType;

            return varCopy;
        }
    }
}
